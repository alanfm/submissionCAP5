public class WorkQueueFrontier {
    private final QueueManager queueManager;
    private final Scheduler scheduler;

    public WorkQueueFrontier(QueueManager queueManager, Scheduler scheduler) {
        this.queueManager = queueManager;
        this.scheduler = scheduler;
    }

    public void schedule(CrawlURI curi) {
        scheduler.schedule(curi);
    }

    public String findReadyClassKey() {
        return queueManager.findReadyClassKey();
    }

    public void processPending() {
        queueManager.processPending();
    }
}

class QueueManager {
    private final Map<String, WorkQueue> queues;

    public QueueManager() {
        this.queues = new HashMap<>();
    }

    public String findReadyClassKey() {
        // Lógica simplificada para encontrar a chave da fila pronta
        for (Map.Entry<String, WorkQueue> entry : queues.entrySet()) {
            if (entry.getValue().isReady()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void processPending() {
        // Lógica simplificada para processar itens pendentes
        for (WorkQueue queue : queues.values()) {
            queue.processPendingItems();
        }
    }

    public void addQueue(String key, WorkQueue queue) {
        queues.put(key, queue);
    }
}

class Scheduler {
    public void schedule(CrawlURI curi) {
        // Lógica simplificada para agendar uma URI
        System.out.println("Scheduling URI: " + curi.getURI());
    }
}

class WorkQueue {
    private final List<CrawlURI> pendingItems;

    public WorkQueue() {
        this.pendingItems = new ArrayList<>();
    }

    public boolean isReady() {
        // Simulação de verificação de prontidão
        return !pendingItems.isEmpty();
    }

    public void processPendingItems() {
        // Processamento de itens pendentes
        while (!pendingItems.isEmpty()) {
            CrawlURI item = pendingItems.remove(0);
            System.out.println("Processing: " + item.getURI());
        }
    }

    public void addItem(CrawlURI item) {
        pendingItems.add(item);
    }
}

class CrawlURI {
    private final String uri;

    public CrawlURI(String uri) {
        this.uri = uri;
    }

    public String getURI() {
        return uri;
    }
}