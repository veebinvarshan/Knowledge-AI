package com.enterprise.platform.core.correlation;

public final class RequestContextHolder {

    private static final ThreadLocal<RequestContext> HOLDER = new ThreadLocal<>();

    private RequestContextHolder() {}

    public static RequestContext getContext() {
        return HOLDER.get();
    }

    public static void setContext(RequestContext context) {
        HOLDER.set(context);
    }

    public static void clearContext() {
        HOLDER.remove();
    }
}
