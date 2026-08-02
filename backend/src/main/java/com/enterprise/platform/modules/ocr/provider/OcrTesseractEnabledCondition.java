package com.enterprise.platform.modules.ocr.provider;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OcrTesseractEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabledStr = context.getEnvironment().getProperty("platform.ocr.enabled");
        boolean enabled = enabledStr == null || Boolean.parseBoolean(enabledStr);
        String provider = context.getEnvironment().getProperty("platform.ocr.provider", "tesseract");
        return enabled && "tesseract".equalsIgnoreCase(provider);
    }
}
