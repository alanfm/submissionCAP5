public class LoadMessageThread extends Thread {
    private final MessageLoader messageLoader;
    private final QueueManager queueManager;
    private final UIUpdater uiUpdater;
    private final ExceptionHandler exceptionHandler;

    public LoadMessageThread(FolderInfo folderInfo) {
        super("Load Message Thread - " + folderInfo.getFolderID());
        this.queueManager = new QueueManager();
        this.messageLoader = new MessageLoader(folderInfo, queueManager);
        this.uiUpdater = new UIUpdater(folderInfo);
        this.exceptionHandler = new ExceptionHandler(folderInfo.getLogger());
    }

    @Override
    public void run() {
        while (!isStopped()) {
            try {
                messageLoader.loadWaitingMessages();
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
            try {
                sleep(getUpdateCheckMilliseconds());
            } catch (InterruptedException ie) {
                // Ignora interrupções
            }
        }
    }

    public void addMessage(MessageProxy mp) {
        queueManager.addMessage(mp);
    }

    public void stopThread() {
        setStopped(true);
    }

    // Getters e Setters
    private boolean isStopped() { return stopped; }
    private int getUpdateCheckMilliseconds() { return updateCheckMilliseconds; }
}

public class MessageLoader {
    private final FolderInfo folderInfo;
    private final QueueManager queueManager;

    public MessageLoader(FolderInfo folderInfo, QueueManager queueManager) {
        this.folderInfo = folderInfo;
        this.queueManager = queueManager;
    }

    public void loadWaitingMessages() {
        List<MessageProxy> messages = queueManager.retrieveNextBatch();
        for (MessageProxy mp : messages) {
            if (!mp.isLoaded()) {
                mp.loadTableInfo();
            }
            if (mp.needsRefresh()) {
                mp.refreshMessage();
            }
            folderInfo.fetch(new MessageInfo[]{mp.getMessageInfo()}, folderInfo.getFetchProfile());
        }
    }
}

public class QueueManager {
    private final List<MessageProxy> loadQueue = new LinkedList<>();
    private final List<MessageProxy> priorityLoadQueue = new LinkedList<>();

    public synchronized void addMessage(MessageProxy mp) {
        if (!priorityLoadQueue.contains(mp)) {
            priorityLoadQueue.add(mp);
        }
        loadQueue.remove(mp);
    }

    public synchronized List<MessageProxy> retrieveNextBatch() {
        List<MessageProxy> batch = new ArrayList<>(priorityLoadQueue);
        batch.addAll(loadQueue);
        priorityLoadQueue.clear();
        loadQueue.clear();
        return batch;
    }
}

public class UIUpdater {
    private final FolderInfo folderInfo;

    public UIUpdater(FolderInfo folderInfo) {
        this.folderInfo = folderInfo;
    }

    public void fireMessageLoadedEvent(int type, int numMessages, int max) {
        folderInfo.getFolderDisplayUI().handleMessageLoaded(
            new MessageLoadedEvent(this, type, numMessages, max)
        );
    }
}

public class ExceptionHandler {
    private final Logger logger;

    public ExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    public void handleException(Exception e) {
        if (logger.isLoggable(java.util.logging.Level.WARNING)) {
            logger.log(java.util.logging.Level.WARNING, "Erro ao carregar mensagens", e);
        }
    }
}