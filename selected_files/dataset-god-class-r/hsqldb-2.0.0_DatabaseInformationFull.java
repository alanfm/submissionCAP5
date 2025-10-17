// Classe dedicada à geração de tabelas de metadados
class MetadataTableGenerator {
    public static Table generateCacheInfo(Session session, Database database) {
        Table t = createBlankTable("SYSTEM_CACHEINFO");
        addColumn(t, "CACHE_FILE", "CHARACTER_DATA");
        addColumn(t, "MAX_CACHE_COUNT", "CARDINAL_NUMBER");
        addColumn(t, "MAX_CACHE_BYTES", "CARDINAL_NUMBER");
        addColumn(t, "CACHE_SIZE", "CARDINAL_NUMBER");
        addColumn(t, "CACHE_BYTES", "CARDINAL_NUMBER");

        PersistentStore store = session.sessionData.getRowStore(t);
        Iterator tables = database.schemaManager.databaseObjectIterator(SchemaObject.TABLE);

        while (tables.hasNext()) {
            Table table = (Table) tables.next();
            if (!table.isText() || !isAccessibleTable(session, table)) {
                continue;
            }

            Object[] row = t.getEmptyRowData();
            row[0] = database.getCatalogName().name;
            row[1] = table.getSchemaName().name;
            row[2] = table.getName().name;

            t.insertSys(store, row);
        }
        return t;
    }

    public static Table generateRoutinePrivileges(Session session, Database database) {
        Table t = createBlankTable("ROUTINE_PRIVILEGES");
        addColumn(t, "SPECIFIC_CATALOG", "SQL_IDENTIFIER");
        addColumn(t, "SPECIFIC_SCHEMA", "SQL_IDENTIFIER");
        addColumn(t, "SPECIFIC_NAME", "SQL_IDENTIFIER");
        addColumn(t, "ROUTINE_CATALOG", "SQL_IDENTIFIER");
        addColumn(t, "ROUTINE_SCHEMA", "SQL_IDENTIFIER");
        addColumn(t, "ROUTINE_NAME", "SQL_IDENTIFIER");

        PersistentStore store = session.sessionData.getRowStore(t);
        Iterator routines = database.schemaManager.databaseObjectIterator(SchemaObject.ROUTINE);

        while (routines.hasNext()) {
            Routine routine = (Routine) routines.next();
            if (!session.getGrantee().isAccessible(routine)) {
                continue;
            }

            Object[] row = t.getEmptyRowData();
            row[0] = database.getCatalogName().name;
            row[1] = routine.getSchemaName().name;
            row[2] = routine.getName().name;

            t.insertSys(store, row);
        }
        return t;
    }

    private static Table createBlankTable(String tableName) {
        Table t = new Table(tableName);
        t.createPrimaryKeyConstraint(tableName, new int[]{0}, false);
        return t;
    }

    private static void addColumn(Table t, String columnName, String dataType) {
        t.addColumn(columnName, dataType);
    }

    private static boolean isAccessibleTable(Session session, Table table) {
        return session.getGrantee().isFullyAccessibleByRole(table.getName());
    }
}

// Versão refatorada da classe principal
public class DatabaseInformationFull extends DatabaseInformationMain {
    private final Database database;

    public DatabaseInformationFull(Database db) {
        super(db);
        this.database = db;
    }

    @Override
    protected Table generateTable(Session session, int tableIndex) {
        switch (tableIndex) {
            case SYSTEM_CACHEINFO:
                return MetadataTableGenerator.generateCacheInfo(session, database);
            case ROUTINE_PRIVILEGES:
                return MetadataTableGenerator.generateRoutinePrivileges(session, database);
            default:
                throw new IllegalArgumentException("Tabela desconhecida: " + tableIndex);
        }
    }
}