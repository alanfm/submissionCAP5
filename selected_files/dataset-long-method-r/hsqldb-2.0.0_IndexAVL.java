import java.util.Iterator;

public class IndexAVL {
    private AVLTreeManager treeManager;
    private IndexMetadata indexMetadata;
    private TransactionManager transactionManager;

    public IndexAVL(HsqlName name, long id, TableBase table, int[] columns,
                    boolean[] descending, boolean[] nullsLast, Type[] colTypes,
                    boolean pk, boolean unique, boolean constraint, boolean forward) {
        this.indexMetadata = new IndexMetadata(name, id, table, columns, descending, nullsLast, colTypes, pk, unique, constraint, forward);
        this.treeManager = new AVLTreeManager();
        this.transactionManager = new TransactionManager();
    }

    public void insert(Session session, PersistentStore store, Row row) {
        NodeAVL node = treeManager.createNode(row);
        if (transactionManager.canInsert(session, store, node)) {
            treeManager.insert(store, node);
        }
    }

    public void delete(Session session, PersistentStore store, Row row) {
        NodeAVL node = treeManager.findNode(row);
        if (transactionManager.canDelete(session, store, node)) {
            treeManager.delete(store, node);
        }
    }

    public Iterator find(Session session, PersistentStore store, Object[] searchData, int[] colMap, int matchCount, int compareType) {
        NodeAVL node = treeManager.findNode(searchData, colMap, matchCount, compareType);
        return transactionManager.filterReadableRows(session, store, node);
    }

    public Iterator firstRow(Session session, PersistentStore store) {
        NodeAVL firstNode = treeManager.getFirstNode(store);
        return transactionManager.filterReadableRows(session, store, firstNode);
    }

    public Iterator lastRow(Session session, PersistentStore store) {
        NodeAVL lastNode = treeManager.getLastNode(store);
        return transactionManager.filterReadableRows(session, store, lastNode);
    }
}

public class AVLTreeManager {
    public NodeAVL createNode(Row row) {
        // Lógica para criar um novo nó com base na linha.
        return new NodeAVL(row);
    }

    public void insert(PersistentStore store, NodeAVL node) {
        // Lógica para inserir um nó na árvore AVL.
    }

    public void delete(PersistentStore store, NodeAVL node) {
        // Lógica para remover um nó da árvore AVL.
    }

    public NodeAVL findNode(Object[] searchData, int[] colMap, int matchCount, int compareType) {
        // Lógica para encontrar um nó com base nos critérios de busca.
        return null;
    }

    public NodeAVL getFirstNode(PersistentStore store) {
        // Lógica para obter o primeiro nó da árvore AVL.
        return null;
    }

    public NodeAVL getLastNode(PersistentStore store) {
        // Lógica para obter o último nó da árvore AVL.
        return null;
    }
}

public class IndexMetadata {
    private final HsqlName name;
    private final long persistenceId;
    private final TableBase table;
    private final int[] columns;
    private final boolean[] descending;
    private final boolean[] nullsLast;
    private final Type[] colTypes;
    private final boolean isPK;
    private final boolean isUnique;
    private final boolean isConstraint;
    private final boolean isForward;

    public IndexMetadata(HsqlName name, long persistenceId, TableBase table, int[] columns,
                         boolean[] descending, boolean[] nullsLast, Type[] colTypes,
                         boolean isPK, boolean isUnique, boolean isConstraint, boolean isForward) {
        this.name = name;
        this.persistenceId = persistenceId;
        this.table = table;
        this.columns = columns;
        this.descending = descending;
        this.nullsLast = nullsLast;
        this.colTypes = colTypes;
        this.isPK = isPK;
        this.isUnique = isUnique;
        this.isConstraint = isConstraint;
        this.isForward = isForward;
    }

    public HsqlName getName() {
        return name;
    }

    public long getPersistenceId() {
        return persistenceId;
    }

    public TableBase getTable() {
        return table;
    }

    public int[] getColumns() {
        return columns;
    }

    public boolean[] getDescending() {
        return descending;
    }

    public boolean[] getNullsLast() {
        return nullsLast;
    }

    public Type[] getColTypes() {
        return colTypes;
    }

    public boolean isPK() {
        return isPK;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public boolean isConstraint() {
        return isConstraint;
    }

    public boolean isForward() {
        return isForward;
    }
}

import java.util.Iterator;

public class TransactionManager {
    public boolean canInsert(Session session, PersistentStore store, NodeAVL node) {
        // Lógica para verificar se a inserção é permitida.
        return true;
    }

    public boolean canDelete(Session session, PersistentStore store, NodeAVL node) {
        // Lógica para verificar se a remoção é permitida.
        return true;
    }

    public Iterator filterReadableRows(Session session, PersistentStore store, NodeAVL node) {
        // Lógica para filtrar linhas legíveis com base nas permissões da sessão.
        return null;
    }
}

public class NodeAVL {
    private Row row;
    private NodeAVL left;
    private NodeAVL right;
    private NodeAVL parent;
    private int balance;

    public NodeAVL(Row row) {
        this.row = row;
    }

    public Row getRow() {
        return row;
    }

    public NodeAVL getLeft() {
        return left;
    }

    public void setLeft(NodeAVL left) {
        this.left = left;
    }

    public NodeAVL getRight() {
        return right;
    }

    public void setRight(NodeAVL right) {
        this.right = right;
    }

    public NodeAVL getParent() {
        return parent;
    }

    public void setParent(NodeAVL parent) {
        this.parent = parent;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}