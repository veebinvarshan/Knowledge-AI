package com.enterprise.platform.modules.ocr;

import com.enterprise.platform.modules.ocr.preprocessing.PdfPageRenderer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class SearchablePdfDetectionTest {

    @Test
    void testSearchablePdfReturnsTrue() throws Exception {
        // GIVEN: Create a PDF containing searchable text using PDFBox
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("This is searchable text in the PDF document that is long enough to exceed the fifty character heuristic threshold for OCR skipping.");
                contentStream.endText();
            }
            doc.save(os);
        }

        PdfPageRenderer renderer = new PdfPageRenderer();

        // WHEN
        boolean searchable = renderer.isSearchablePdf(new ByteArrayInputStream(os.toByteArray()));

        // THEN
        assertTrue(searchable);
    }
}
