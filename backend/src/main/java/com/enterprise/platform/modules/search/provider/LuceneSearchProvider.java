package com.enterprise.platform.modules.search.provider;

import com.enterprise.platform.core.config.properties.LuceneProperties;
import com.enterprise.platform.modules.search.domain.SearchDocument;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Component
public class LuceneSearchProvider implements SearchProvider {

    private static final Logger log = LoggerFactory.getLogger(LuceneSearchProvider.class);

    private final Directory directory;
    private final Analyzer analyzer;
    private IndexWriter indexWriter;

    public LuceneSearchProvider(LuceneProperties properties) {
        this.analyzer = new StandardAnalyzer();
        Directory dir = null;
        try {
            if ("mem".equalsIgnoreCase(properties.indexDir())) {
                dir = new ByteBuffersDirectory();
            } else {
                File dirFile = new File(properties.indexDir());
                if (!dirFile.exists()) {
                    dirFile.mkdirs();
                }
                dir = FSDirectory.open(Paths.get(properties.indexDir()));
            }
        } catch (Exception e) {
            log.warn("Failed to initialize FSDirectory. Falling back to ByteBuffersDirectory: {}", e.getMessage());
            dir = new ByteBuffersDirectory();
        }
        this.directory = dir;

        try {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setSimilarity(new BM25Similarity());
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            this.indexWriter = new IndexWriter(directory, config);
            this.indexWriter.commit();
        } catch (IOException e) {
            log.error("Failed to initialize Lucene IndexWriter", e);
        }
    }

    @Override
    public boolean supports(String indexType) {
        return "LEXICAL".equalsIgnoreCase(indexType);
    }

    @Override
    public int getPriority() {
        return 2; // Lucene priority
    }

    @Override
    public synchronized void index(SearchDocument doc) throws Exception {
        // Enforce transactional replacement: delete old documents first
        delete(doc.getDocumentId());

        Document luceneDoc = new Document();
        luceneDoc.add(new StringField("documentId", doc.getDocumentId().toString(), Field.Store.YES));
        luceneDoc.add(new StringField("versionId", doc.getVersionId().toString(), Field.Store.YES));
        luceneDoc.add(new StringField("tenantId", doc.getTenantId(), Field.Store.YES));
        luceneDoc.add(new StringField("currentVersion", String.valueOf(doc.getCurrentVersion()), Field.Store.YES));
        luceneDoc.add(new StringField("permissionHash", doc.getPermissionHash() != null ? doc.getPermissionHash() : "", Field.Store.YES));
        
        // Stored & Indexed text fields
        luceneDoc.add(new TextField("title", doc.getTitle() != null ? doc.getTitle() : "", Field.Store.YES));
        luceneDoc.add(new TextField("filename", doc.getFilename() != null ? doc.getFilename() : "", Field.Store.YES));
        luceneDoc.add(new TextField("mimeType", doc.getMimeType() != null ? doc.getMimeType() : "", Field.Store.YES));
        luceneDoc.add(new TextField("language", doc.getLanguage() != null ? doc.getLanguage() : "", Field.Store.YES));
        luceneDoc.add(new TextField("normalizedText", doc.getNormalizedText() != null ? doc.getNormalizedText() : "", Field.Store.YES));
        
        indexWriter.addDocument(luceneDoc);
        indexWriter.commit();
    }

    @Override
    public synchronized void delete(UUID documentId) throws Exception {
        indexWriter.deleteDocuments(new Term("documentId", documentId.toString()));
        indexWriter.commit();
    }

    @Override
    public SearchResult search(String query, String tenantId, String permissionHash, int limit) throws Exception {
        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new BM25Similarity());

            // Build Query
            QueryParser parser = new QueryParser("normalizedText", analyzer);
            parser.setAllowLeadingWildcard(true);
            Query userQuery = parser.parse(query);

            // Pre-filtering Security Queries (Enforced at search engine level)
            BooleanQuery.Builder finalQuery = new BooleanQuery.Builder();
            finalQuery.add(userQuery, BooleanClause.Occur.MUST);
            finalQuery.add(new TermQuery(new Term("tenantId", tenantId)), BooleanClause.Occur.FILTER);
            
            if (permissionHash != null && !permissionHash.isBlank()) {
                finalQuery.add(new TermQuery(new Term("permissionHash", permissionHash)), BooleanClause.Occur.FILTER);
            }
            finalQuery.add(new TermQuery(new Term("currentVersion", "true")), BooleanClause.Occur.FILTER);

            TopDocs topDocs = searcher.search(finalQuery.build(), limit);
            List<SearchResult.Match> matches = new ArrayList<>();
            Map<String, Map<String, Long>> facets = new HashMap<>();

            // Build Facets mappings
            Map<String, Long> mimeFacets = new HashMap<>();
            Map<String, Long> langFacets = new HashMap<>();

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                UUID docId = UUID.fromString(doc.get("documentId"));
                UUID verId = UUID.fromString(doc.get("versionId"));
                String tId = doc.get("tenantId");
                String title = doc.get("title");
                String filename = doc.get("filename");
                String mime = doc.get("mimeType");
                String lang = doc.get("language");
                String fullText = doc.get("normalizedText");

                // Facet increment
                mimeFacets.put(mime, mimeFacets.getOrDefault(mime, 0L) + 1);
                langFacets.put(lang, langFacets.getOrDefault(lang, 0L) + 1);

                // Highlight Generation (At query time, phrase matches with boundaries)
                List<String> highlights = generateHighlights(fullText, query);

                matches.add(new SearchResult.Match(
                        docId, verId, tId, title, filename, scoreDoc.score, highlights, new HashMap<>()
                ));
            }

            facets.put("mimeType", mimeFacets);
            facets.put("language", langFacets);

            return new SearchResult(matches, topDocs.totalHits.value, facets);
        }
    }

    private List<String> generateHighlights(String text, String query) {
        if (text == null || text.isBlank() || query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        
        List<String> snippets = new ArrayList<>();
        String lowerText = text.toLowerCase();
        String[] terms = query.toLowerCase().split("\\s+");

        for (String term : terms) {
            // Clean wildcards from term for position match
            String cleanTerm = term.replace("*", "").replace("?", "");
            if (cleanTerm.isBlank()) continue;

            int idx = lowerText.indexOf(cleanTerm);
            if (idx != -1) {
                int start = Math.max(0, idx - 30);
                int end = Math.min(text.length(), idx + cleanTerm.length() + 30);
                
                String rawSnippet = text.substring(start, end);
                String highlightSnippet = rawSnippet.replaceAll("(?i)" + cleanTerm, "<em>$0</em>");
                snippets.add("..." + highlightSnippet + "...");
                break; // Limit to one highlight snippet per document
            }
        }
        
        return snippets;
    }

    public synchronized void close() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
        if (directory != null) {
            directory.close();
        }
    }
}
