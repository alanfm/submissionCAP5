// Classe dedicada à manipulação de métodos Java
class JavaMethodHandler {
    public static Method getMethod(String methodName, Routine routine, boolean[] hasConnection, boolean returnsTable) {
        int i = methodName.lastIndexOf('.');
        if (i == -1) {
            throw new IllegalArgumentException("Nome de método inválido: " + methodName);
        }

        String className = methodName.substring(0, i);
        String methodname = methodName.substring(i + 1);

        try {
            Class<?> cl = Class.forName(className);
            Method[] methods = cl.getDeclaredMethods();

            for (Method method : methods) {
                if (method.getName().equals(methodname)) {
                    hasConnection[0] = method.getParameterTypes()[0].equals(java.sql.Connection.class);
                    return method;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Classe não encontrada: " + className, e);
        }

        return null;
    }

    public static void validateSQLImpact(Database database, OrderedHashSet references) {
        for (int i = 0; i < references.size(); i++) {
            HsqlName name = (HsqlName) references.get(i);
            if (name.type == SchemaObject.SPECIFIC_ROUTINE) {
                Routine routine = (Routine) database.schemaManager.getSchemaObject(name);
                if (routine.dataImpact == Routine.READS_SQL || routine.dataImpact == Routine.MODIFIES_SQL) {
                    throw new RuntimeException("Impacto de SQL inválido para a rotina: " + name.name);
                }
            }
        }
    }
}

// Classe dedicada ao gerenciamento de parâmetros
class ParameterManager {
    public static void addParameter(Routine routine, Method method) {
        Class<?>[] params = method.getParameterTypes();
        int offset = routine.javaMethodWithConnection ? 1 : 0;

        for (int j = offset; j < params.length; j++) {
            Type methodParamType = Types.getParameterSQLType(params[j]);
            ColumnSchema param = new ColumnSchema(null, methodParamType, !params[j].isPrimitive(), false, null);
            routine.addParameter(param);
        }
    }
}

// Versão refatorada da classe principal
public class Routine implements SchemaObject {
    private HsqlName name;
    private HsqlName specificName;
    private Type returnType;
    private final int routineType;
    private int dataImpact = CONTAINS_SQL;
    private Method javaMethod;
    private boolean javaMethodWithConnection;
    private HashMappedList parameterList = new HashMappedList();

    public Routine(int type) {
        this.routineType = type;
        this.returnType = Type.SQL_ALL_TYPES;
    }

    public void setName(HsqlName name) {
        this.name = name;
    }

    public void setMethod(Method method) {
        this.javaMethod = method;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public void addParameter(ColumnSchema param) {
        parameterList.add(param.getName(), param);
    }

    public static Routine newRoutine(Session session, Method method) {
        Routine routine = new Routine(SchemaObject.FUNCTION);
        ParameterManager.addParameter(routine, method);

        routine.setMethod(method);
        routine.setReturnType(Types.getParameterSQLType(method.getReturnType()));
        routine.javaMethodWithConnection = method.getParameterTypes()[0].equals(java.sql.Connection.class);

        routine.resolve(session);
        return routine;
    }

    public static void createRoutines(Session session, HsqlName schema, String name) {
        Method[] methods = JavaMethodHandler.getMethod(name, null, new boolean[]{false}, false).getDeclaringClass().getDeclaredMethods();
        Routine[] routines = new Routine[methods.length];

        for (int i = 0; i < methods.length; i++) {
            routines[i] = newRoutine(session, methods[i]);
        }

        HsqlName routineName = session.database.nameManager.newHsqlName(schema, name, true, SchemaObject.FUNCTION);
        for (Routine routine : routines) {
            routine.setName(routineName);
            session.database.schemaManager.addSchemaObject(routine);
        }
    }

    public void resolve(Session session) {
        // Lógica de resolução da rotina
    }

    public void checkNoSQLData(Database database) {
        JavaMethodHandler.validateSQLImpact(database, getReferences());
    }

    private OrderedHashSet getReferences() {
        // Retorna as referências da rotina
        return new OrderedHashSet();
    }
}