package org.exoplatform.container.monitor;

import java.util.Map;
import java.util.Iterator;

public class ActionData {
    private final String portal;
    private final String page;
    private final String requestType;
    private final long handleTime;
    private final String parameters;
    private String error;

    public ActionData(String portal, String page, String requestType, long handleTime, Map<String, Object> params) {
        this.portal = portal;
        this.page = page;
        this.requestType = requestType;
        this.handleTime = handleTime;
        this.parameters = ParameterFormatter.formatParameters(params);
    }

    public String getPortal() {
        return portal;
    }

    public String getPage() {
        return page;
    }

    public String getRequestType() {
        return requestType;
    }

    public long getHandleTime() {
        return handleTime;
    }

    public String getParameters() {
        return parameters;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

class ParameterFormatter {
    public static String formatParameters(Map<String, Object> params) {
        StringBuilder formattedParams = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            formattedParams.append("{");
            String key = entry.getKey();
            Object value = entry.getValue();
            formattedParams.append(key).append("=");

            if (key.startsWith("password")) {
                formattedParams.append("**************");
            } else if (value instanceof String[]) {
                String[] values = (String[]) value;
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) formattedParams.append(", ");
                    formattedParams.append(values[i]);
                }
            } else {
                formattedParams.append(value);
            }

            formattedParams.append("} ");
        }
        return formattedParams.toString();
    }
}