public class MessageFolderWebHandler {
    private final MessageFolderManager folderManager;
    private final MessageFolderPreparer folderPreparer;
    private final PermissionValidator permissionValidator;

    public MessageFolderWebHandler() {
        this.folderManager = new MessageFolderManager();
        this.folderPreparer = new MessageFolderPreparer();
        this.permissionValidator = new PermissionValidator();
    }

    public void prepareAdd(GenericRequest request) throws Exception {
        permissionValidator.ensureAuthenticated(request);
        permissionValidator.ensureCanUseMessage(request);
        folderManager.prepareAdd(request);
    }

    public void prepareList(GenericRequest request) throws Exception {
        permissionValidator.ensureAuthenticated(request);
        permissionValidator.ensureCanUseMessage(request);

        if (!MVNForumConfig.getEnablePrivateMessage()) {
            throw new IllegalStateException("Private messages are disabled.");
        }

        int memberID = OnlineUserManager.getInstance().getOnlineUser(request).getMemberID();
        folderPreparer.prepareFolderList(request, memberID);
    }

    public void processDelete(GenericRequest request) throws Exception {
        permissionValidator.ensureAuthenticated(request);
        permissionValidator.ensureCanUseMessage(request);

        int memberID = OnlineUserManager.getInstance().getOnlineUser(request).getMemberID();
        String folderName = GenericParamUtil.getParameterSafe(request, "folder", true);

        folderManager.processDelete(folderName, memberID);
    }

    public void processUpdateOrder(GenericRequest request) throws Exception {
        permissionValidator.ensureAuthenticated(request);
        permissionValidator.ensureCanUseMessage(request);

        int memberID = OnlineUserManager.getInstance().getOnlineUser(request).getMemberID();
        String folderName = GenericParamUtil.getParameterSafe(request, "folder", true);
        String action = GenericParamUtil.getParameterSafe(request, "action", true);

        folderManager.processUpdateOrder(folderName, memberID, action);
    }
}

public class MessageFolderManager {
    public void prepareAdd(GenericRequest request) throws Exception {
        OnlineUser onlineUser = OnlineUserManager.getInstance().getOnlineUser(request);
        String folderName = GenericParamUtil.getParameterSafe(request, "folder", true);

        int folderOrder = DAODelegate.getMaxFolderOrder(onlineUser.getMemberID()) + 1;
        if (folderOrder < 10) folderOrder = 10; // Reserve order for special folders

        DAODelegate.createFolder(folderName, onlineUser.getMemberID(), folderOrder);
    }

    public void processDelete(String folderName, int memberID) throws Exception {
        if (isDefaultFolder(folderName)) {
            throw new BadInputException("Cannot delete default folder: " + folderName);
        }

        DAODelegate.deleteFolder(folderName, memberID);
    }

    public void processUpdateOrder(String folderName, int memberID, String action) throws Exception {
        if ("up".equalsIgnoreCase(action)) {
            DAODelegate.decreaseFolderOrder(folderName, memberID);
        } else if ("down".equalsIgnoreCase(action)) {
            DAODelegate.increaseFolderOrder(folderName, memberID);
        } else {
            throw new BadInputException("Unknown action: " + action);
        }
    }

    private boolean isDefaultFolder(String folderName) {
        return MVNForumConstant.MESSAGE_FOLDER_INBOX.equalsIgnoreCase(folderName) ||
               MVNForumConstant.MESSAGE_FOLDER_SENT.equalsIgnoreCase(folderName) ||
               MVNForumConstant.MESSAGE_FOLDER_DRAFT.equalsIgnoreCase(folderName) ||
               MVNForumConstant.MESSAGE_FOLDER_TRASH.equalsIgnoreCase(folderName);
    }
}

public class MessageFolderPreparer {
    public void prepareFolderList(GenericRequest request, int memberID) throws Exception {
        Collection<MessageFolderBean> messageFolderBeans = DAODelegate.getMessageFolders(memberID);

        for (MessageFolderBean folder : messageFolderBeans) {
            int messageCount = DAODelegate.getNumberOfMessages(memberID, folder.getFolderName());
            int unreadMessageCount = DAODelegate.getNumberOfUnreadMessages(memberID, folder.getFolderName());

            folder.setMessageCount(messageCount);
            folder.setUnreadMessageCount(unreadMessageCount);
        }

        request.setAttribute("FolderMessageBeans", messageFolderBeans);
    }
}

public class PermissionValidator {
    public void ensureAuthenticated(GenericRequest request) throws AuthenticationException {
        OnlineUser onlineUser = OnlineUserManager.getInstance().getOnlineUser(request);
        MVNForumPermission permission = onlineUser.getPermission();
        permission.ensureIsAuthenticated();
    }

    public void ensureCanUseMessage(GenericRequest request) throws AuthenticationException {
        OnlineUser onlineUser = OnlineUserManager.getInstance().getOnlineUser(request);
        MVNForumPermission permission = onlineUser.getPermission();
        permission.ensureCanUseMessage();
    }
}

public class DAODelegate {
    public static void createFolder(String folderName, int memberID, int folderOrder) throws DatabaseException {
        DAOFactory.getMessageFolderDAO().create(folderName, memberID, folderOrder);
    }

    public static void deleteFolder(String folderName, int memberID) throws DatabaseException {
        DAOFactory.getMessageFolderDAO().delete(folderName, memberID);
    }

    public static void decreaseFolderOrder(String folderName, int memberID) throws DatabaseException {
        DAOFactory.getMessageFolderDAO().decreaseFolderOrder(folderName, memberID);
    }

    public static void increaseFolderOrder(String folderName, int memberID) throws DatabaseException {
        DAOFactory.getMessageFolderDAO().increaseFolderOrder(folderName, memberID);
    }

    public static Collection<MessageFolderBean> getMessageFolders(int memberID) throws DatabaseException {
        return DAOFactory.getMessageFolderDAO().getMessageFolders_inMember(memberID);
    }

    public static int getNumberOfMessages(int memberID, String folderName) throws DatabaseException {
        return DAOFactory.getMessageDAO().getNumberOfNonPublicMessages_inMember_inFolder(memberID, folderName);
    }

    public static int getNumberOfUnreadMessages(int memberID, String folderName) throws DatabaseException {
        return DAOFactory.getMessageDAO().getNumberOfUnreadNonPublicMessages_inMember_inFolder(memberID, folderName);
    }

    public static int getMaxFolderOrder(int memberID) throws DatabaseException {
        return DAOFactory.getMessageFolderDAO().getMaxFolderOrder(memberID);
    }
}