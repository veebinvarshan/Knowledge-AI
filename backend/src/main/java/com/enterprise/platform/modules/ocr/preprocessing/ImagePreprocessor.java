package com.enterprise.platform.modules.ocr.preprocessing;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

@Component
public class ImagePreprocessor {

    public BufferedImage preprocess(BufferedImage image) {
        // Implement pipeline stages individually
        BufferedImage grayscaled = toGrayscale(image);
        BufferedImage contrastEnhanced = enhanceContrast(grayscaled);
        BufferedImage deskewed = deskew(contrastEnhanced);
        return deskewed;
    }

    public BufferedImage toGrayscale(BufferedImage image) {
        BufferedImage grayscale = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayscale.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return grayscale;
    }

    public BufferedImage enhanceContrast(BufferedImage image) {
        // Simple contrast stretch algorithm
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = new Color(image.getRGB(x, y));
                int r = Math.min(255, (int)(c.getRed() * 1.2));
                int g = Math.min(255, (int)(c.getGreen() * 1.2));
                int b = Math.min(255, (int)(c.getBlue() * 1.2));
                result.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        return result;
    }

    public BufferedImage deskew(BufferedImage image) {
        // Simulated deskewing (fine angle rotation using AffineTransform)
        double angleRad = Math.toRadians(0.5); // Mock a deskew angle of 0.5 degrees
        double sin = Math.abs(Math.sin(angleRad));
        double cos = Math.abs(Math.cos(angleRad));
        int w = image.getWidth();
        int h = image.getHeight();
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newW, newH, image.getType());
        Graphics2D g = rotated.createGraphics();
        
        // Anti-aliasing
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        
        AffineTransform at = new AffineTransform();
        at.translate((newW - w) / 2.0, (newH - h) / 2.0);
        at.rotate(angleRad, w / 2.0, h / 2.0);
        g.setTransform(at);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        return rotated;
    }
}
