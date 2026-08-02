package com.enterprise.platform.infrastructure.virusscan;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ClamAvEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enabledStr = context.getEnvironment().getProperty("platform.virusscan.enabled");
        boolean enabled = enabledStr == null || Boolean.parseBoolean(enabledStr);
        String provider = context.getEnvironment().getProperty("platform.virusscan.provider", "clamav");
        return enabled && "clamav".equalsIgnoreCase(provider);
    }
}
