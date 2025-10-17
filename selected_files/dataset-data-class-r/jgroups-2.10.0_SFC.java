public class SFC extends Protocol {
    private final StateManager stateManager;
    private final EventProcessor eventProcessor;

    public SFC() {
        this.stateManager = new StateManager();
        this.eventProcessor = new EventProcessor(stateManager);
    }

    @Override
    public Object up(Event evt) {
        return eventProcessor.processUpEvent(evt);
    }

    @Override
    public Object down(Event evt) {
        return eventProcessor.processDownEvent(evt);
    }
}

class StateManager {
    private Membership currentMembership;
    private Map<Address, Long> timestamps;

    public StateManager() {
        this.timestamps = new HashMap<>();
    }

    public void updateMembership(Membership membership) {
        this.currentMembership = membership;
    }

    public void updateTimestamp(Address address, long timestamp) {
        timestamps.put(address, timestamp);
    }

    public Membership getCurrentMembership() {
        return currentMembership;
    }

    public Map<Address, Long> getTimestamps() {
        return timestamps;
    }
}

class EventProcessor {
    private final StateManager stateManager;

    public EventProcessor(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    public Object processUpEvent(Event evt) {
        switch (evt.getType()) {
            case Event.MSG:
                Message msg = (Message) evt.getArg();
                processMessage(msg);
                break;
            case Event.VIEW_CHANGE:
                Membership membership = (Membership) evt.getArg();
                stateManager.updateMembership(membership);
                break;
        }
        return null;
    }

    public Object processDownEvent(Event evt) {
        switch (evt.getType()) {
            case Event.MSG:
                Message msg = (Message) evt.getArg();
                processMessage(msg);
                break;
        }
        return null;
    }

    private void processMessage(Message msg) {
        Address sender = msg.getSrc();
        long timestamp = System.currentTimeMillis();
        stateManager.updateTimestamp(sender, timestamp);
        // Processar a mensagem conforme necessário
    }
}