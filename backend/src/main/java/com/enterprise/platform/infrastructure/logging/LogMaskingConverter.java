package com.enterprise.platform.infrastructure.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogMaskingConverter extends ClassicConverter {

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
        "(?i)(password|jwt|refresh_token|token|authorization|apikey|api-key|secret)\\s*[:=]\\s*[\"']?([^\"'\\s,\\n\\r]+)[\"']?",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null || message.isEmpty()) {
            return message;
        }

        Matcher matcher = SENSITIVE_PATTERN.matcher(message);
        if (matcher.find()) {
            StringBuilder sb = new StringBuilder();
            int lastIndex = 0;
            matcher.reset();
            while (matcher.find()) {
                sb.append(message, lastIndex, matcher.start(2));
                sb.append("[MASKED]");
                lastIndex = matcher.end(2);
            }
            sb.append(message.substring(lastIndex));
            return sb.toString();
        }
        return message;
    }
}
