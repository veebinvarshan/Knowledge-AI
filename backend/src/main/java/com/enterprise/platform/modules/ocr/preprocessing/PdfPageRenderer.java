package com.enterprise.platform.modules.ocr.preprocessing;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.InputStream;

@Component
public class PdfPageRenderer {

    /**
     * Heuristic to check if PDF already contains searchable text content.
     */
    public boolean isSearchablePdf(InputStream inputStream) {
        try (PDDocument doc = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            return text != null && text.trim().length() > 50; // Text threshold
        } catch (Exception e) {
            return false;
        }
    }

    public int getPageCount(InputStream inputStream) {
        try (PDDocument doc = Loader.loadPDF(inputStream.readAllBytes())) {
            return doc.getNumberOfPages();
        } catch (Exception e) {
            return 0;
        }
    }

    public BufferedImage renderPage(InputStream inputStream, int pageIndex, int dpi) throws Exception {
        try (PDDocument doc = Loader.loadPDF(inputStream.readAllBytes())) {
            if (pageIndex < 0 || pageIndex >= doc.getNumberOfPages()) {
                throw new IllegalArgumentException("Invalid page index: " + pageIndex);
            }
            PDFRenderer renderer = new PDFRenderer(doc);
            return renderer.renderImageWithDPI(pageIndex, dpi, ImageType.RGB);
        }
    }
}
