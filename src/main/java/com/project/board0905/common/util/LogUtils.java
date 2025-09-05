package com.project.board0905.common.util;

import org.slf4j.MDC;

public class LogUtils {
    public static String getTraceId() {
        return MDC.get(RequestCorrelationFilter.TRACE_ID);
    }
}
