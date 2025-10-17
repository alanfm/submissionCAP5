public class Routine implements SchemaObject {
    private final HsqlName name;
    private final Type returnType;
    private final List<ColumnSchema> parameters = new ArrayList<>();
    private final int routineType;
    private final int dataImpact;
    private final boolean isLibraryRoutine;

    public Routine(HsqlName name, Type returnType, List<ColumnSchema> parameters, int routineType, int dataImpact, boolean isLibraryRoutine) {
        this.name = name;
        this.returnType = returnType;
        this.parameters.addAll(parameters);
        this.routineType = routineType;
        this.dataImpact = dataImpact;
        this.isLibraryRoutine = isLibraryRoutine;
    }

    public HsqlName getName() {
        return name;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<ColumnSchema> getParameters() {
        return parameters;
    }

    public int getRoutineType() {
        return routineType;
    }

    public int getDataImpact() {
        return dataImpact;
    }

    public boolean isLibraryRoutine() {
        return isLibraryRoutine;
    }
}

public class RoutineFactory {
    public static Routine newRoutine(Session session, Method method) {
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        HsqlName routineName = session.database.nameManager.newHsqlName(null, methodName, true, SchemaObject.FUNCTION);

        List<ColumnSchema> parameters = new ArrayList<>();
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Type paramType = Types.getParameterSQLType(paramTypes[i]);
            ColumnSchema param = new ColumnSchema(null, paramType, !paramTypes[i].isPrimitive(), false, null);
            parameters.add(param);
        }

        Type returnType = Types.getParameterSQLType(method.getReturnType());
        int routineType = SchemaObject.FUNCTION;
        int dataImpact = Routine.NO_SQL;
        boolean isLibraryRoutine = className.equals("java.lang.Math");

        return new Routine(routineName, returnType, parameters, routineType, dataImpact, isLibraryRoutine);
    }

    public static void createRoutines(Session session, HsqlName schema, String name) {
        Method[] methods = RoutineResolver.getMethods(name);
        Routine[] routines = new Routine[methods.length];

        for (int i = 0; i < methods.length; i++) {
            routines[i] = newRoutine(session, methods[i]);
            routines[i].getName().schema = schema;
            session.database.schemaManager.addSchemaObject(routines[i]);
        }
    }
}

public class RoutineResolver {
    public static Method[] getMethods(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot == -1) {
            throw new RuntimeException("Invalid method name: " + name);
        }

        String className = name.substring(0, lastDot);
        String methodName = name.substring(lastDot + 1);

        try {
            Class<?> clazz = Class.forName(className);
            return clazz.getDeclaredMethods();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found: " + className, e);
        }
    }

    public static void checkNoSQLData(Database database, OrderedHashSet set) {
        for (int i = 0; i < set.size(); i++) {
            HsqlName name = (HsqlName) set.get(i);
            if (name.type == SchemaObject.SPECIFIC_ROUTINE) {
                Routine routine = (Routine) database.schemaManager.getSchemaObject(name);
                if (routine.getDataImpact() == Routine.READS_SQL || routine.getDataImpact() == Routine.MODIFIES_SQL) {
                    throw new RuntimeException("Invalid SQL operation in routine: " + name);
                }
            }
        }
    }
}

public class RoutineInvoker {
    public static Result invokeJavaMethod(Session session, Method method, Object[] data) {
        try {
            Object returnValue = method.invoke(null, data);
            if (method.getReturnType().equals(void.class)) {
                return Result.newPSMResult(null);
            }

            Type returnType = Types.getParameterSQLType(method.getReturnType());
            returnValue = returnType.convertJavaToSQL(session, returnValue);
            return Result.newPSMResult(returnValue);
        } catch (Exception e) {
            return Result.newErrorResult(new RuntimeException("Error invoking method: " + method.getName(), e));
        }
    }
}

public class RoutineValidator {
    public static void validateRoutine(Routine routine) {
        if (routine.getDataImpact() == Routine.MODIFIES_SQL) {
            throw new RuntimeException("Routine modifies SQL data: " + routine.getName());
        }
    }
}