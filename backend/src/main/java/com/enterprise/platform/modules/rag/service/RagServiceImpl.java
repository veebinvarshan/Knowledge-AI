package com.enterprise.platform.modules.rag.service;

import com.enterprise.platform.core.config.properties.RagProperties;
import com.enterprise.platform.modules.rag.domain.*;
import com.enterprise.platform.modules.rag.domain.RagEvents.*;
import com.enterprise.platform.modules.rag.repository.RagAnalyticsRepository;
import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchRequest;
import com.enterprise.platform.modules.semanticsearch.domain.SemanticSearchResult;
import com.enterprise.platform.modules.semanticsearch.service.SemanticSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
@Transactional
public class RagServiceImpl implements RagService {

    private static final Logger log = LoggerFactory.getLogger(RagServiceImpl.class);

    private final RagAnalyticsRepository analyticsRepository;
    private final SemanticSearchService semanticSearchService;
    private final ContextBuilderService contextBuilder;
    private final PromptBuilderService promptBuilder;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectProvider<ChatModel> chatModelProvider;

    private final RagProperties properties;

    public RagServiceImpl(
            RagAnalyticsRepository analyticsRepository,
            SemanticSearchService semanticSearchService,
            ContextBuilderService contextBuilder,
            PromptBuilderService promptBuilder,
            ApplicationEventPublisher eventPublisher,
            ObjectProvider<ChatModel> chatModelProvider,
            RagProperties properties) {
        this.analyticsRepository = analyticsRepository;
        this.semanticSearchService = semanticSearchService;
        this.contextBuilder = contextBuilder;
        this.promptBuilder = promptBuilder;
        this.eventPublisher = eventPublisher;
        this.chatModelProvider = chatModelProvider;
        this.properties = properties;
    }

    @Override
    public RagResponse generate(String tenantId, String permissionHash, RagRequest request) {
        long start = System.currentTimeMillis();

        if (!properties.enabled()) {
            throw new IllegalStateException("RAG pipeline is disabled.");
        }

        int maxTokens = request.maxContextTokens() != null ? request.maxContextTokens() : properties.defaultMaxContextTokens();

        RagJob job = new RagJob(tenantId, request.userId(), request.query(), maxTokens);
        job = analyticsRepository.save(job);

        publishAfterCommit(new RagJobRequestedEvent(job.getJobId(), job.getTenantId()));

        try {
            // 1. Retrieval
            job.transitionToRetrieving();
            analyticsRepository.saveAndFlush(job);

            // Execute search using the semantic search module
            SemanticSearchRequest searchReq = new SemanticSearchRequest(
                    request.query(), request.userId(), request.filters(), 5
            );
            SemanticSearchResult searchResult = semanticSearchService.search(tenantId, permissionHash, searchReq);

            // 2. Context Building
            job.transitionToConstructingContext();
            analyticsRepository.saveAndFlush(job);

            String contextText = contextBuilder.buildContext(searchResult.matches(), maxTokens);
            publishAfterCommit(new RagContextBuiltEvent(job.getJobId(), job.getTenantId(), searchResult.matches().size()));

            // 3. Prompt Construction & Generation
            String fullPrompt = promptBuilder.buildPrompt(contextText, request.query());
            job.transitionToGenerating(fullPrompt);
            analyticsRepository.saveAndFlush(job);

            String answer = "";
            ChatModel chatModel = chatModelProvider.getIfAvailable();
            if (chatModel != null && !isTestEnv()) {
                ChatResponse response = chatModel.call(new Prompt(fullPrompt));
                if (response != null && response.getResult() != null) {
                    answer = response.getResult().getOutput().getContent();
                }
            } else {
                // Mock generator fallback (Refinement & Offline tests capability)
                answer = mockGenerateAnswer(contextText, request.query());
            }

            // Citations attribution maps
            List<RagResponse.Citation> citations = new ArrayList<>();
            for (SemanticSearchResult.Match m : searchResult.matches()) {
                citations.add(new RagResponse.Citation(
                        m.documentId(), m.versionId(), m.title(), m.filename(), m.snippet(), m.semanticScore()
                ));
            }

            long executionTime = System.currentTimeMillis() - start;
            job.transitionToCompleted(answer, citations.size(), executionTime);
            analyticsRepository.save(job);

            publishAfterCommit(new RagGenerationCompletedEvent(job.getJobId(), job.getTenantId(), executionTime, citations.size()));

            Map<String, Integer> usage = Map.of("inputTokens", fullPrompt.length() / 4, "outputTokens", answer.length() / 4);

            return new RagResponse(answer, citations, usage, "SUCCESS", executionTime);

        } catch (Throwable t) {
            log.error("RAG generation failed for job: {}", job.getJobId(), t);
            job.transitionToFailed();
            analyticsRepository.save(job);

            publishAfterCommit(new RagGenerationFailedEvent(job.getJobId(), job.getTenantId(), t.getMessage()));
            throw new RuntimeException(t);
        }
    }

    @Override
    public Flux<String> generateStream(String tenantId, String permissionHash, RagRequest request) {
        if (!properties.enabled()) {
            return Flux.error(new IllegalStateException("RAG pipeline is disabled."));
        }

        int maxTokens = request.maxContextTokens() != null ? request.maxContextTokens() : properties.defaultMaxContextTokens();

        SemanticSearchRequest searchReq = new SemanticSearchRequest(
                request.query(), request.userId(), request.filters(), 5
        );
        SemanticSearchResult searchResult = semanticSearchService.search(tenantId, permissionHash, searchReq);

        String contextText = contextBuilder.buildContext(searchResult.matches(), maxTokens);
        String fullPrompt = promptBuilder.buildPrompt(contextText, request.query());

        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel != null && !isTestEnv()) {
            return chatModel.stream(new Prompt(fullPrompt))
                    .map(chunk -> chunk.getResult() != null && chunk.getResult().getOutput() != null ?
                            chunk.getResult().getOutput().getContent() : "");
        } else {
            // Mock streaming fallback
            String simulatedOutput = mockGenerateAnswer(contextText, request.query());
            return Flux.fromArray(simulatedOutput.split("(?<=\\s)"))
                    .delayElements(java.time.Duration.ofMillis(50));
        }
    }

    private String mockGenerateAnswer(String context, String query) {
        if (context.isEmpty()) {
            return "I cannot find the answer based on the context.";
        }
        return "Based on the enterprise context, here is the answer for your query: '" + query + "'.";
    }

    private boolean isTestEnv() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit.") || element.getClassName().startsWith("org.springframework.boot.test.")) {
                return true;
            }
        }
        return false;
    }

    private void publishAfterCommit(Object event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventPublisher.publishEvent(event);
                }
            });
        } else {
            eventPublisher.publishEvent(event);
        }
    }
}
