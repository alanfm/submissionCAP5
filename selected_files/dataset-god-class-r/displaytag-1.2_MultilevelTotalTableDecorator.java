// Classe dedicada à manipulação de totais
class TotalCalculator {
    public static Object calculateTotal(Column column, Object total, Object value) {
        if (value instanceof Number && total instanceof Number) {
            double oldValue = ((Number) total).doubleValue();
            double newValue = ((Number) value).doubleValue();
            return oldValue + newValue;
        }
        throw new UnsupportedOperationException("Cannot add a value of " + value + " in column " + column.getHeaderCell().getTitle());
    }

    public static Object getTotalForColumn(int columnNumber, int startRow, int stopRow, TableModel tableModel) {
        List<Row> window = tableModel.getRowListFull().subList(startRow, stopRow + 1);
        Object total = null;

        for (Row row : window) {
            ColumnIterator columnIterator = row.getColumnIterator(tableModel.getHeaderCellList());
            while (columnIterator.hasNext()) {
                Column column = columnIterator.nextColumn();
                if (column.getHeaderCell().getColumnNumber() == columnNumber) {
                    try {
                        Object value = column.getValue(false);
                        if (value != null && !TagConstants.EMPTY_STRING.equals(value)) {
                            total = calculateTotal(column, total, value);
                        }
                    } catch (Exception e) {
                        // Log and handle exception
                    }
                }
            }
        }
        return total;
    }
}

// Classe dedicada à formatação de células
class CellFormatter {
    public static String formatTotal(HeaderCell header, Object total) {
        Object displayValue = total;
        if (header.getColumnDecorators().length > 0) {
            for (DisplaytagColumnDecorator decorator : header.getColumnDecorators()) {
                try {
                    displayValue = decorator.decorate(total, null, null);
                } catch (Exception e) {
                    // Log and handle exception
                }
            }
        }
        return displayValue != null ? displayValue.toString() : "";
    }

    public static String getTotalsTdOpen(HeaderCell header, String totalClass) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(TagConstants.TAG_OPEN).append(TagConstants.TAGNAME_COLUMN);

        String cssClass = header.getHtmlAttributes().get("class") != null ? header.getHtmlAttributes().get("class").toString() : "";
        if (!cssClass.isEmpty() || totalClass != null) {
            buffer.append(" class=\"");
            if (!cssClass.isEmpty()) {
                buffer.append(cssClass).append(" ");
            }
            if (totalClass != null) {
                buffer.append(totalClass);
            }
            buffer.append("\"");
        }

        buffer.append(TagConstants.TAG_CLOSE);
        return buffer.toString();
    }
}

// Versão refatorada da classe principal
public class MultilevelTotalTableDecorator extends TableDecorator {
    private boolean containsTotaledColumns = false;
    private Map<Integer, GroupTotals> groupNumberToGroupTotal = new HashMap<>();
    private int innermostGroup;
    private Log logger = LogFactory.getLog(MultilevelTotalTableDecorator.class);

    public String formatTotal(HeaderCell header, Object total) {
        return CellFormatter.formatTotal(header, total);
    }

    public String getTotalsTdOpen(HeaderCell header, String totalClass) {
        return CellFormatter.getTotalsTdOpen(header, totalClass);
    }

    public Object getTotalForColumn(int columnNumber, int startRow, int stopRow) {
        return TotalCalculator.getTotalForColumn(columnNumber, startRow, stopRow, this.tableModel);
    }

    public void printTotals(GroupTotals groupTotals, int currentRow, StringBuffer out) {
        List<HeaderCell> headerCells = tableModel.getHeaderCellList();
        out.append(groupTotals.totalsRowOpen);

        for (HeaderCell headerCell : headerCells) {
            if (groupTotals.columnNumber == headerCell.getColumnNumber()) {
                String currentLabel = getCellValue(groupTotals.columnNumber, groupTotals.firstRowOfCurrentSet);
                out.append(getTotalsTdOpen(headerCell, groupTotals.totalLabelClass));
                out.append(formatTotal(headerCell, currentLabel));
            } else if (headerCell.isTotaled()) {
                Object total = getTotalForColumn(headerCell.getColumnNumber(), groupTotals.firstRowOfCurrentSet, currentRow);
                out.append(getTotalsTdOpen(headerCell, groupTotals.totalValueClass));
                out.append(formatTotal(headerCell, total));
            } else {
                out.append(getTotalsTdOpen(headerCell, ""));
            }
            out.append(TagConstants.TAG_OPENCLOSING).append(TagConstants.TAGNAME_COLUMN).append(TagConstants.TAG_CLOSE);
        }
        out.append("</tr>");
    }

    public String totalAllRows() {
        if (!containsTotaledColumns) {
            return "";
        }

        StringBuilder output = new StringBuilder();
        output.append(TagConstants.TAG_OPEN).append(TagConstants.TAGNAME_ROW)
              .append(" class=\"grandtotal-row\"").append(TagConstants.TAG_CLOSE);

        for (HeaderCell headerCell : tableModel.getHeaderCellList()) {
            if (headerCell.isTotaled()) {
                Object total = getTotalForColumn(headerCell.getColumnNumber(), 0, getListIndex());
                output.append(getTotalsTdOpen(headerCell, "grandtotal-sum"));
                output.append(formatTotal(headerCell, total));
            } else {
                output.append(getTotalsTdOpen(headerCell, "grandtotal-nosum"));
            }
            output.append(TagConstants.TAG_OPENCLOSING).append(TagConstants.TAGNAME_COLUMN).append(TagConstants.TAG_CLOSE);
        }
        output.append("</tr>");
        return output.toString();
    }

    private String getCellValue(int columnNumber, int rowNumber) {
        Row row = tableModel.getRowListFull().get(rowNumber);
        ColumnIterator columnIterator = row.getColumnIterator(tableModel.getHeaderCellList());

        while (columnIterator.hasNext()) {
            Column column = columnIterator.nextColumn();
            if (column.getHeaderCell().getColumnNumber() == columnNumber) {
                try {
                    column.initialize();
                    return column.getChoppedAndLinkedValue();
                } catch (Exception e) {
                    logger.error("Error: " + e.getMessage(), e);
                }
            }
        }
        throw new RuntimeException("Unable to find column " + columnNumber + " in the list of columns");
    }
}