public class WatchWebHandler {
    private final RequestProcessor requestProcessor;
    private final ResponseGenerator responseGenerator;

    public WatchWebHandler(RequestProcessor requestProcessor, ResponseGenerator responseGenerator) {
        this.requestProcessor = requestProcessor;
        this.responseGenerator = responseGenerator;
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> parameters = requestProcessor.processRequestParameters(request);
        if (!requestProcessor.validateParameters(parameters)) {
            responseGenerator.generateErrorResponse(response, "Invalid parameters");
            return;
        }
        String result = requestProcessor.executeQuery(parameters);
        responseGenerator.generateSuccessResponse(response, result);
    }
}

class RequestProcessor {
    public Map<String, String> processRequestParameters(HttpServletRequest request) {
        Map<String, String> parameters = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            parameters.put(paramName, request.getParameter(paramName));
        }
        return parameters;
    }

    public boolean validateParameters(Map<String, String> parameters) {
        // Lógica de validação simplificada
        return parameters.containsKey("action") && parameters.containsKey("id");
    }

    public String executeQuery(Map<String, String> parameters) {
        // Simulação de consulta ao banco de dados
        String action = parameters.get("action");
        String id = parameters.get("id");
        return "Executed " + action + " for ID: " + id;
    }
}

class ResponseGenerator {
    public void generateSuccessResponse(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(message);
        } catch (IOException e) {
            throw new RuntimeException("Error generating success response", e);
        }
    }

    public void generateErrorResponse(HttpServletResponse response, String errorMessage) {
        try {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(errorMessage);
        } catch (IOException e) {
            throw new RuntimeException("Error generating error response", e);
        }
    }
}