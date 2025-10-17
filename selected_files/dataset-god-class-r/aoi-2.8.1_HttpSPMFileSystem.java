// Classe dedicada à manipulação de requisições HTTP
class HttpRequestHandler {
    public static HttpResponse handleRequest(HttpRequest request, HttpSPMFileSystem fileSystem) {
        String method = request.getMethod();
        String path = request.getPath();

        if ("GET".equalsIgnoreCase(method)) {
            return fileSystem.readFile(path);
        } else if ("POST".equalsIgnoreCase(method)) {
            String content = request.getBody();
            return fileSystem.writeFile(path, content);
        } else {
            return new HttpResponse(405, "Method Not Allowed");
        }
    }
}

// Classe dedicada à validação de caminhos de arquivos
class PathValidator {
    public static boolean isValidPath(String path) {
        // Lógica para validar o caminho do arquivo
        return path != null && !path.contains("..") && !path.startsWith("/");
    }
}

// Classe dedicada à manipulação de arquivos no sistema de arquivos
class FileSystemManager {
    private final String basePath;

    public FileSystemManager(String basePath) {
        this.basePath = basePath;
    }

    public String readFileContent(String path) throws IOException {
        File file = new File(basePath, path);
        if (!file.exists()) {
            throw new FileNotFoundException("Arquivo não encontrado: " + path);
        }
        return new String(Files.readAllBytes(file.toPath()));
    }

    public void writeFileContent(String path, String content) throws IOException {
        File file = new File(basePath, path);
        Files.write(file.toPath(), content.getBytes());
    }
}

// Versão refatorada da classe principal
public class HttpSPMFileSystem {
    private final FileSystemManager fileSystemManager;

    public HttpSPMFileSystem(String basePath) {
        this.fileSystemManager = new FileSystemManager(basePath);
    }

    public HttpResponse readFile(String path) {
        if (!PathValidator.isValidPath(path)) {
            return new HttpResponse(400, "Caminho inválido");
        }

        try {
            String content = fileSystemManager.readFileContent(path);
            return new HttpResponse(200, content);
        } catch (FileNotFoundException e) {
            return new HttpResponse(404, "Arquivo não encontrado");
        } catch (IOException e) {
            return new HttpResponse(500, "Erro ao ler o arquivo");
        }
    }

    public HttpResponse writeFile(String path, String content) {
        if (!PathValidator.isValidPath(path)) {
            return new HttpResponse(400, "Caminho inválido");
        }

        try {
            fileSystemManager.writeFileContent(path, content);
            return new HttpResponse(200, "Arquivo gravado com sucesso");
        } catch (IOException e) {
            return new HttpResponse(500, "Erro ao gravar o arquivo");
        }
    }

    public HttpResponse handleRequest(HttpRequest request) {
        return HttpRequestHandler.handleRequest(request, this);
    }
}