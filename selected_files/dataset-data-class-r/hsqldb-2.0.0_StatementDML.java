public class StatementDML {
    private final ForeignKeyHandler foreignKeyHandler;
    private final TriggerHandler triggerHandler;

    public StatementDML() {
        this.foreignKeyHandler = new ForeignKeyHandler();
        this.triggerHandler = new TriggerHandler();
    }

    public int update(Session session, Table table, RowSetNavigatorDataChange updateList) {
        return new UpdateOperation(session, table, updateList, foreignKeyHandler, triggerHandler).execute();
    }

    public int delete(Session session, Table table, RowSetNavigatorDataChange deleteList) {
        return new DeleteOperation(session, table, deleteList, foreignKeyHandler, triggerHandler).execute();
    }
}

class ForeignKeyHandler {
    public void performReferentialActions(Session session, Table refTable, RowSetNavigatorDataChange navigator, Row refRow, Object[] data, int[] path) {
        // Lógica para lidar com ações referenciais, como CASCADE, SET_NULL, etc.
        if (refRow.getId() == data[0]) {
            // Exemplo de lógica simplificada
            System.arraycopy(refRow.getData(), 0, data, 0, data.length);
        }
    }
}

class TriggerHandler {
    public void handleTriggers(Session session, Table table, OrderedHashSet set, boolean write) {
        // Lógica para disparar gatilhos antes ou depois de operações
        for (TriggerDef td : table.getTriggerList()) {
            if (write && td.isAfterTrigger()) {
                set.add(td.getName());
            }
        }
    }
}

class UpdateOperation {
    private final Session session;
    private final Table table;
    private final RowSetNavigatorDataChange updateList;
    private final ForeignKeyHandler foreignKeyHandler;
    private final TriggerHandler triggerHandler;

    public UpdateOperation(Session session, Table table, RowSetNavigatorDataChange updateList, ForeignKeyHandler foreignKeyHandler, TriggerHandler triggerHandler) {
        this.session = session;
        this.table = table;
        this.updateList = updateList;
        this.foreignKeyHandler = foreignKeyHandler;
        this.triggerHandler = triggerHandler;
    }

    public int execute() {
        OrderedHashSet extraUpdateTables = new OrderedHashSet();
        triggerHandler.handleTriggers(session, table, extraUpdateTables, false);

        while (updateList.hasNext()) {
            Row row = updateList.getNextRow();
            Object[] data = row.getData();
            foreignKeyHandler.performReferentialActions(session, table, updateList, row, data, null);
            table.insertSingleRow(session, table.getRowStore(session), data, null);
        }

        return updateList.getSize();
    }
}

class DeleteOperation {
    private final Session session;
    private final Table table;
    private final RowSetNavigatorDataChange deleteList;
    private final ForeignKeyHandler foreignKeyHandler;
    private final TriggerHandler triggerHandler;

    public DeleteOperation(Session session, Table table, RowSetNavigatorDataChange deleteList, ForeignKeyHandler foreignKeyHandler, TriggerHandler triggerHandler) {
        this.session = session;
        this.table = table;
        this.deleteList = deleteList;
        this.foreignKeyHandler = foreignKeyHandler;
        this.triggerHandler = triggerHandler;
    }

    public int execute() {
        OrderedHashSet extraDeleteTables = new OrderedHashSet();
        triggerHandler.handleTriggers(session, table, extraDeleteTables, true);

        while (deleteList.hasNext()) {
            Row row = deleteList.getNextRow();
            Object[] data = row.getData();
            foreignKeyHandler.performReferentialActions(session, table, deleteList, row, data, null);
            table.deleteRow(session, row);
        }

        return deleteList.getSize();
    }
}