package com.enterprise.platform.modules.embedding.provider;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class EmbeddingGeminiEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabledStr = context.getEnvironment().getProperty("platform.embedding.enabled");
        boolean enabled = enabledStr == null || Boolean.parseBoolean(enabledStr);
        String provider = context.getEnvironment().getProperty("platform.embedding.provider", "gemini");
        return enabled && "gemini".equalsIgnoreCase(provider);
    }
}
