import java.io.IOException;
import java.util.Locale;
import org.apache.commons.fileupload.FileItem;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

public class PmAttachmentWebHandler {
    private AttachmentProcessor attachmentProcessor;
    private PermissionValidator permissionValidator;
    private FileHandler fileHandler;
    private DatabaseManager databaseManager;

    public PmAttachmentWebHandler() {
        this.attachmentProcessor = new AttachmentProcessor();
        this.permissionValidator = new PermissionValidator();
        this.fileHandler = new FileHandler();
        this.databaseManager = new DatabaseManager();
    }

    public void processUpload(HttpServletRequest request, HttpServletResponse response)
            throws IOException, AuthenticationException, BadInputException, DatabaseException {
        Locale locale = I18nUtil.getLocaleInRequest(request);
        permissionValidator.validateAttachmentFeature(locale);
        OnlineUser onlineUser = permissionValidator.authenticateUser(request);
        permissionValidator.ensureCanAddMessageAttachment(onlineUser);

        int sizeMax = permissionValidator.getMaxAttachmentSize(onlineUser);
        String tempDir = MVNForumConfig.getTempDir();
        List<FileItem> fileItems = fileHandler.parseRequest(request, sizeMax, tempDir);

        ParsedRequest parsedRequest = attachmentProcessor.parseFileItems(fileItems);
        attachmentProcessor.validateMessageOwnership(parsedRequest.getMessageID(), onlineUser.getMemberID());

        int attachID = databaseManager.createAttachment(parsedRequest, onlineUser);
        fileHandler.storeAttachment(attachID, parsedRequest.getAttachFileItem());
        databaseManager.updateMessageAttachCount(parsedRequest.getMessageID());
    }

    public void downloadAttachment(HttpServletRequest request, HttpServletResponse response)
            throws IOException, AuthenticationException, ObjectNotFoundException, BadInputException {
        Locale locale = I18nUtil.getLocaleInRequest(request);
        permissionValidator.validateAttachmentFeature(locale);
        OnlineUser onlineUser = permissionValidator.authenticateUser(request);

        int attachID = ParamUtil.getParameterInt(request, "attach");
        int messageID = ParamUtil.getParameterInt(request, "message");

        PmAttachmentBean pmAttachBean = databaseManager.getPmAttachment(attachID);
        MessageBean messageBean = databaseManager.getMessage(messageID);
        permissionValidator.validateMessageOwnership(messageBean, onlineUser.getMemberID());

        fileHandler.downloadAttachment(pmAttachBean, response);
        databaseManager.increaseDownloadCount(attachID);
    }

    public void deleteOrphanPmAttachment() throws DatabaseException {
        Collection<PmAttachmentBean> orphanAttachments = databaseManager.getOrphanPmAttachments();
        for (PmAttachmentBean attachment : orphanAttachments) {
            fileHandler.deleteAttachment(attachment.getPmAttachID());
            databaseManager.deletePmAttachment(attachment.getPmAttachID());
        }
    }
}

public class AttachmentProcessor {
    public ParsedRequest parseFileItems(List<FileItem> fileItems) {
        ParsedRequest parsedRequest = new ParsedRequest();
        for (FileItem item : fileItems) {
            if (item.isFormField()) {
                processFormField(item, parsedRequest);
            } else {
                parsedRequest.setAttachFileItem(item);
            }
        }
        return parsedRequest;
    }

    private void processFormField(FileItem item, ParsedRequest parsedRequest) {
        String fieldName = item.getFieldName();
        try {
            String content = item.getString("utf-8");
            switch (fieldName) {
                case "messageID":
                    parsedRequest.setMessageID(Integer.parseInt(content));
                    break;
                case "AttachDesc":
                    parsedRequest.setAttachDesc(content);
                    break;
                default:
                    // Ignorar campos desconhecidos
                    break;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao processar campo de formulário: " + fieldName, e);
        }
    }

    public void validateMessageOwnership(int messageID, int memberID) throws BadInputException {
        if (!DAOFactory.getMessageDAO().isMessageOwner(messageID, memberID)) {
            throw new BadInputException("Esta mensagem não pertence ao usuário.");
        }
    }
}

public class PermissionValidator {
    public void validateAttachmentFeature(Locale locale) {
        if (!MVNForumConfig.getEnableMessageAttachment()) {
            String localizedMessage = MVNForumResourceBundle.getString(locale, "java.lang.IllegalStateException.message_attachment_is_disabled");
            throw new IllegalStateException(localizedMessage);
        }
    }

    public OnlineUser authenticateUser(HttpServletRequest request) throws AuthenticationException {
        OnlineUser onlineUser = OnlineUserManager.getInstance().getOnlineUser(request);
        MVNForumPermission permission = onlineUser.getPermission();
        permission.ensureIsAuthenticated();
        permission.ensureCanUseMessage();
        return onlineUser;
    }

    public void ensureCanAddMessageAttachment(OnlineUser onlineUser) {
        MVNForumPermission permission = onlineUser.getPermission();
        permission.ensureCanAddMessageAttachment();
    }

    public int getMaxAttachmentSize(OnlineUser onlineUser) {
        return permission.canAdminSystem() ? Integer.MAX_VALUE : MVNForumConfig.getMaxMessageAttachmentSize();
    }

    public void validateMessageOwnership(MessageBean messageBean, int memberID) throws BadInputException {
        if (!messageBean.isOwnedBy(memberID)) {
            throw new BadInputException("Esta mensagem não pertence ao usuário.");
        }
    }
}

public class FileHandler {
    private BinaryStorageService binaryStorageService = MvnCoreServiceFactory.getMvnCoreService().getBinaryStorageService();

    public List<FileItem> parseRequest(HttpServletRequest request, int sizeMax, String tempDir)
            throws IOException {
        try {
            return FileUploadParserService.parseRequest(request, sizeMax, 100000, tempDir, "UTF-8");
        } catch (FileUploadException ex) {
            throw new IOException("Erro ao fazer upload do arquivo: " + ex.getMessage(), ex);
        }
    }

    public void storeAttachment(int attachID, FileItem attachFileItem) throws IOException {
        try {
            binaryStorageService.storeData(
                    BinaryStorageService.CATEGORY_PM_ATTACHMENT,
                    String.valueOf(attachID),
                    attachFileItem.getName(),
                    attachFileItem.getInputStream(),
                    attachFileItem.getSize(),
                    0, 0, attachFileItem.getContentType(),
                    null
            );
        } catch (Exception ex) {
            throw new IOException("Erro ao salvar o arquivo de anexo.", ex);
        }
    }

    public void downloadAttachment(PmAttachmentBean pmAttachBean, HttpServletResponse response)
            throws IOException {
        response.setContentType(pmAttachBean.getPmAttachMimeType());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + pmAttachBean.getPmAttachFilename() + "\"");

        try (InputStream inputStream = binaryStorageService.getInputStream(
                BinaryStorageService.CATEGORY_PM_ATTACHMENT,
                String.valueOf(pmAttachBean.getPmAttachID()),
                null);
             OutputStream outputStream = response.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception ex) {
            throw new IOException("Erro ao baixar o arquivo de anexo.", ex);
        }
    }

    public void deleteAttachment(int attachID) {
        try {
            binaryStorageService.deleteData(BinaryStorageService.CATEGORY_PM_ATTACHMENT, String.valueOf(attachID), null);
        } catch (IOException e) {
            // Logar o erro, mas não lançar exceção
            System.err.println("Erro ao deletar anexo com ID: " + attachID);
        }
    }
}

public class DatabaseManager {
    public PmAttachmentBean getPmAttachment(int attachID) throws ObjectNotFoundException {
        try {
            return DAOFactory.getPmAttachmentDAO().getPmAttachment(attachID);
        } catch (ObjectNotFoundException e) {
            throw new ObjectNotFoundException("Anexo não encontrado com ID: " + attachID);
        }
    }

    public MessageBean getMessage(int messageID) throws ObjectNotFoundException {
        try {
            return DAOFactory.getMessageDAO().getMessage(messageID);
        } catch (ObjectNotFoundException e) {
            throw new ObjectNotFoundException("Mensagem não encontrada com ID: " + messageID);
        }
    }

    public boolean isMessageOwner(int messageID, int memberID) {
        return DAOFactory.getMessageDAO().isMessageOwner(messageID, memberID);
    }

    public int createAttachment(ParsedRequest parsedRequest, OnlineUser onlineUser) throws DatabaseException {
        return DAOFactory.getPmAttachmentDAO().create(
                onlineUser.getMemberID(),
                parsedRequest.getAttachFilename(),
                parsedRequest.getAttachFileSize(),
                parsedRequest.getAttachMimeType(),
                parsedRequest.getAttachDesc(),
                null, // IP de criação
                java.util.Date.from(java.time.Instant.now()),
                java.util.Date.from(java.time.Instant.now()),
                0, // Contagem de downloads
                0, // Opções
                0  // Status
        );
    }

    public void updateMessageAttachCount(int messageID) throws DatabaseException {
        DAOFactory.getMessageDAO().updateAttachCount(messageID, DAOFactory.getPmAttachMessageDAO().getNumberOfBeans_inMessage(messageID));
    }

    public Collection<PmAttachmentBean> getOrphanPmAttachments() throws DatabaseException {
        return DAOFactory.getPmAttachmentDAO().getOrphanPmAttachments();
    }

    public void deletePmAttachment(int attachID) throws DatabaseException {
        DAOFactory.getPmAttachmentDAO().delete(attachID);
    }
}

