public class MessageStatistics {
    private int totalMessages;
    private int unreadMessages;
    private long lastMessageTimestamp;

    public MessageStatistics(int totalMessages, int unreadMessages, long lastMessageTimestamp) {
        this.totalMessages = totalMessages;
        this.unreadMessages = unreadMessages;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public int getTotalMessages() {
        return totalMessages;
    }

    public int getUnreadMessages() {
        return unreadMessages;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void incrementTotalMessages() {
        this.totalMessages++;
    }

    public void incrementUnreadMessages() {
        this.unreadMessages++;
    }

    public void markMessageAsRead() {
        if (unreadMessages > 0) {
            this.unreadMessages--;
        }
    }

    public void updateLastMessageTimestamp(long timestamp) {
        this.lastMessageTimestamp = timestamp;
    }

    public String getSummary() {
        return "Total Messages: " + totalMessages + ", Unread Messages: " + unreadMessages;
    }
}