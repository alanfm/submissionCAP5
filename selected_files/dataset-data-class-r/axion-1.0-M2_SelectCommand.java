import java.util.*;

public class SelectCommand {
    private final QueryProcessor queryProcessor;
    private final RowIteratorFactory rowIteratorFactory;

    public SelectCommand(QueryProcessor queryProcessor, RowIteratorFactory rowIteratorFactory) {
        this.queryProcessor = queryProcessor;
        this.rowIteratorFactory = rowIteratorFactory;
    }

    public RowIterator execute(Database db, FromNode from, WhereNode where, List<OrderBy> orderByList) {
        return queryProcessor.processQuery(db, from, where, orderByList);
    }
}

class QueryProcessor {
    private final Map<ColumnIdentifier, Integer> colIdToFieldMap = new HashMap<>();
    private int indexOffset = 0;

    public RowIterator processQuery(Database db, FromNode from, WhereNode where, List<OrderBy> orderByList) {
        try {
            // Process FROM clause
            RowIterator rows = processFromTree(from, db);

            // Process WHERE clause
            if (where != null) {
                rows = applyWhereConditions(rows, where);
            }

            // Apply ORDER BY
            if (orderByList != null && !orderByList.isEmpty()) {
                rows = applyOrderBy(rows, orderByList);
            }

            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Error processing query", e);
        }
    }

    private RowIterator processFromTree(FromNode from, Database db) throws Exception {
        RowIterator leftIter = null, rightIter = null;

        if (from.getLeft() != null) {
            Object leftChild = from.getLeft();
            if (leftChild instanceof FromNode) {
                leftIter = processFromTree((FromNode) leftChild, db);
            } else if (leftChild instanceof TableIdentifier) {
                leftIter = processTable((TableIdentifier) leftChild, db);
            }
        }

        if (from.getRight() != null) {
            Object rightChild = from.getRight();
            if (rightChild instanceof FromNode) {
                rightIter = processFromTree((FromNode) rightChild, db);
            } else if (rightChild instanceof TableIdentifier) {
                rightIter = processTable((TableIdentifier) rightChild, db);
            }
        }

        return mergeRowIterators(leftIter, rightIter);
    }

    private RowIterator processTable(TableIdentifier table, Database db) throws Exception {
        Table t = db.getTable(table);
        if (t == null) {
            throw new Exception("Table " + table + " not found.");
        }
        return new TableRowIterator(t);
    }

    private RowIterator mergeRowIterators(RowIterator left, RowIterator right) {
        // Lógica para mesclar iteradores de linha
        return new MergedRowIterator(left, right);
    }

    private RowIterator applyWhereConditions(RowIterator rows, WhereNode where) {
        return new FilteringRowIterator(rows, new RowDecorator(colIdToFieldMap), where);
    }

    private RowIterator applyOrderBy(RowIterator rows, List<OrderBy> orderByList) {
        ComparatorChain orderChain = generateOrderChain(orderByList);
        List<Row> sortedRows = new ArrayList<>();
        while (rows.hasNext()) {
            sortedRows.add(rows.next());
        }
        sortedRows.sort(orderChain);
        return new ListIteratorRowIterator(sortedRows.listIterator());
    }

    private ComparatorChain generateOrderChain(List<OrderBy> orderByList) {
        ComparatorChain chain = new ComparatorChain();
        for (OrderBy orderBy : orderByList) {
            chain.addComparator(new OrderByComparator(orderBy));
        }
        return chain;
    }
}

class RowIteratorFactory {
    public RowIterator createLiteralIterator(List<Literal> literals) {
        Row literalRow = new SimpleRow(literals.size());
        for (int i = 0; i < literals.size(); i++) {
            literalRow.setValue(i, literals.get(i).getValue());
        }
        return new SingleRowIterator(literalRow);
    }
}

// Classes auxiliares simplificadas
class FromNode {
    private final Object left;
    private final Object right;

    public FromNode(Object left, Object right) {
        this.left = left;
        this.right = right;
    }

    public Object getLeft() {
        return left;
    }

    public Object getRight() {
        return right;
    }
}

class TableIdentifier {
    private final String tableName;

    public TableIdentifier(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}

class WhereNode {
    private final String condition;

    public WhereNode(String condition) {
        this.condition = condition;
    }

    public String getCondition() {
        return condition;
    }
}

class OrderBy {
    private final String column;
    private final boolean ascending;

    public OrderBy(String column, boolean ascending) {
        this.column = column;
        this.ascending = ascending;
    }

    public String getColumn() {
        return column;
    }

    public boolean isAscending() {
        return ascending;
    }
}

class RowIterator {
    public boolean hasNext() {
        return false;
    }

    public Row next() {
        return null;
    }
}

class TableRowIterator extends RowIterator {
    private final Table table;

    public TableRowIterator(Table table) {
        this.table = table;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Row next() {
        return null;
    }
}

class MergedRowIterator extends RowIterator {
    private final RowIterator left;
    private final RowIterator right;

    public MergedRowIterator(RowIterator left, RowIterator right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean hasNext() {
        return left.hasNext() || right.hasNext();
    }

    @Override
    public Row next() {
        return left.hasNext() ? left.next() : right.next();
    }
}

class FilteringRowIterator extends RowIterator {
    private final RowIterator inner;
    private final RowDecorator decorator;
    private final WhereNode condition;

    public FilteringRowIterator(RowIterator inner, RowDecorator decorator, WhereNode condition) {
        this.inner = inner;
        this.decorator = decorator;
        this.condition = condition;
    }

    @Override
    public boolean hasNext() {
        return inner.hasNext();
    }

    @Override
    public Row next() {
        return inner.next();
    }
}

class ListIteratorRowIterator extends RowIterator {
    private final ListIterator<Row> iterator;

    public ListIteratorRowIterator(ListIterator<Row> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Row next() {
        return iterator.next();
    }
}

class RowDecorator {
    private final Map<ColumnIdentifier, Integer> colIdToFieldMap;

    public RowDecorator(Map<ColumnIdentifier, Integer> colIdToFieldMap) {
        this.colIdToFieldMap = colIdToFieldMap;
    }

    public Object getValue(Row row, ColumnIdentifier column) {
        int index = colIdToFieldMap.get(column);
        return row.getValue(index);
    }
}

class ComparatorChain implements Comparator<Row> {
    private final List<Comparator<Row>> comparators = new ArrayList<>();

    public void addComparator(Comparator<Row> comparator) {
        comparators.add(comparator);
    }

    @Override
    public int compare(Row o1, Row o2) {
        for (Comparator<Row> comparator : comparators) {
            int result = comparator.compare(o1, o2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }
}

class OrderByComparator implements Comparator<Row> {
    private final OrderBy orderBy;

    public OrderByComparator(OrderBy orderBy) {
        this.orderBy = orderBy;
    }

    @Override
    public int compare(Row o1, Row o2) {
        Object value1 = o1.getValue(orderBy.getColumn());
        Object value2 = o2.getValue(orderBy.getColumn());
        int result = ((Comparable) value1).compareTo(value2);
        return orderBy.isAscending() ? result : -result;
    }
}