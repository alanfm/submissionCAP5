// Classe dedicada à manipulação de requisições GET
class GetRequestHandler {
    public static HttpResponse handle(HttpRequest request, SessionManager sessionManager) {
        String path = request.getPath();
        if ("/dashboard".equals(path)) {
            return generateDashboardResponse(sessionManager);
        } else if ("/users".equals(path)) {
            return generateUsersResponse(sessionManager);
        }
        return new HttpResponse(404, "Not Found");
    }

    private static HttpResponse generateDashboardResponse(SessionManager sessionManager) {
        // Lógica para gerar resposta do dashboard
        return new HttpResponse(200, "Dashboard Content");
    }

    private static HttpResponse generateUsersResponse(SessionManager sessionManager) {
        // Lógica para gerar resposta de lista de usuários
        return new HttpResponse(200, "Users List");
    }
}

// Classe dedicada à manipulação de requisições POST
class PostRequestHandler {
    public static HttpResponse handle(HttpRequest request, SessionManager sessionManager) {
        String path = request.getPath();
        if ("/login".equals(path)) {
            return authenticateUser(request, sessionManager);
        } else if ("/update".equals(path)) {
            return updateUser(request, sessionManager);
        }
        return new HttpResponse(404, "Not Found");
    }

    private static HttpResponse authenticateUser(HttpRequest request, SessionManager sessionManager) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (sessionManager.authenticate(username, password)) {
            return new HttpResponse(200, "Login Successful");
        }
        return new HttpResponse(401, "Unauthorized");
    }

    private static HttpResponse updateUser(HttpRequest request, SessionManager sessionManager) {
        String userId = request.getParameter("userId");
        String newData = request.getParameter("data");

        if (sessionManager.updateUser(userId, newData)) {
            return new HttpResponse(200, "User Updated");
        }
        return new HttpResponse(400, "Bad Request");
    }
}

// Classe dedicada à validação de entradas
class InputValidator {
    public static boolean validateCredentials(String username, String password) {
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }

    public static boolean validateUpdateData(String userId, String data) {
        return userId != null && !userId.isEmpty() && data != null && !data.isEmpty();
    }
}

// Versão refatorada da classe principal
public class WebadminRequestHandler {
    private final SessionManager sessionManager;

    public WebadminRequestHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public HttpResponse handleRequest(HttpRequest request) {
        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method)) {
            return GetRequestHandler.handle(request, sessionManager);
        } else if ("POST".equalsIgnoreCase(method)) {
            return PostRequestHandler.handle(request, sessionManager);
        } else {
            return new HttpResponse(405, "Method Not Allowed");
        }
    }
}