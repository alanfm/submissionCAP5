public class BaseHashMap {
    private HashIndexManager hashIndexManager;
    private KeyManager keyManager;
    private ValueManager valueManager;
    private AccessManager accessManager;

    public BaseHashMap(int initialCapacity, int keyType, int valueType, boolean hasAccessCount) {
        this.hashIndexManager = new HashIndexManager(initialCapacity);
        this.keyManager = new KeyManager(keyType, initialCapacity);
        this.valueManager = new ValueManager(valueType, initialCapacity);
        this.accessManager = hasAccessCount ? new AccessManager(initialCapacity) : null;
    }

    public void put(Object key, Object value) {
        int hash = hashIndexManager.computeHash(key);
        int lookup = hashIndexManager.getLookup(hash);

        if (lookup < 0) {
            lookup = hashIndexManager.addKey(key);
            keyManager.insertKey(lookup, key);
        }

        valueManager.insertValue(lookup, value);
        if (accessManager != null) {
            accessManager.updateAccess(lookup);
        }
    }

    public Object get(Object key) {
        int hash = hashIndexManager.computeHash(key);
        int lookup = hashIndexManager.getLookup(hash);

        if (lookup >= 0) {
            return valueManager.getValue(lookup);
        }

        return null;
    }

    public void remove(Object key) {
        int hash = hashIndexManager.computeHash(key);
        int lookup = hashIndexManager.getLookup(hash);

        if (lookup >= 0) {
            hashIndexManager.removeKey(lookup);
            keyManager.removeKey(lookup);
            valueManager.removeValue(lookup);
            if (accessManager != null) {
                accessManager.clearAccess(lookup);
            }
        }
    }

    public void clear() {
        hashIndexManager.clear();
        keyManager.clear();
        valueManager.clear();
        if (accessManager != null) {
            accessManager.clear();
        }
    }

    public Iterator iterator() {
        return new HashMapIterator();
    }

    private class HashMapIterator implements Iterator {
        private int currentIndex = -1;

        @Override
        public boolean hasNext() {
            return hashIndexManager.hasNext(currentIndex);
        }

        @Override
        public Object next() {
            currentIndex = hashIndexManager.nextIndex(currentIndex);
            return valueManager.getValue(currentIndex);
        }
    }
}

public class HashIndexManager {
    private HashIndex hashIndex;

    public HashIndexManager(int initialCapacity) {
        this.hashIndex = new HashIndex(initialCapacity);
    }

    public int computeHash(Object key) {
        return key.hashCode();
    }

    public int getLookup(int hash) {
        return hashIndex.getLookup(hash);
    }

    public int addKey(Object key) {
        return hashIndex.addKey(key);
    }

    public void removeKey(int lookup) {
        hashIndex.removeKey(lookup);
    }

    public void clear() {
        hashIndex.clear();
    }

    public boolean hasNext(int currentIndex) {
        return hashIndex.hasNext(currentIndex);
    }

    public int nextIndex(int currentIndex) {
        return hashIndex.nextIndex(currentIndex);
    }
}

public class KeyManager {
    private Object[] objectKeys;
    private int[] intKeys;
    private long[] longKeys;

    public KeyManager(int keyType, int capacity) {
        if (keyType == BaseHashMap.objectKeyOrValue) {
            objectKeys = new Object[capacity];
        } else if (keyType == BaseHashMap.intKeyOrValue) {
            intKeys = new int[capacity];
        } else {
            longKeys = new long[capacity];
        }
    }

    public void insertKey(int index, Object key) {
        if (objectKeys != null) {
            objectKeys[index] = key;
        } else if (intKeys != null) {
            intKeys[index] = (int) key;
        } else {
            longKeys[index] = (long) key;
        }
    }

    public void removeKey(int index) {
        if (objectKeys != null) {
            objectKeys[index] = null;
        } else if (intKeys != null) {
            intKeys[index] = 0;
        } else {
            longKeys[index] = 0;
        }
    }

    public void clear() {
        if (objectKeys != null) {
            java.util.Arrays.fill(objectKeys, null);
        } else if (intKeys != null) {
            java.util.Arrays.fill(intKeys, 0);
        } else {
            java.util.Arrays.fill(longKeys, 0L);
        }
    }
}

public class ValueManager {
    private Object[] objectValues;
    private int[] intValues;
    private long[] longValues;

    public ValueManager(int valueType, int capacity) {
        if (valueType == BaseHashMap.objectKeyOrValue) {
            objectValues = new Object[capacity];
        } else if (valueType == BaseHashMap.intKeyOrValue) {
            intValues = new int[capacity];
        } else {
            longValues = new long[capacity];
        }
    }

    public void insertValue(int index, Object value) {
        if (objectValues != null) {
            objectValues[index] = value;
        } else if (intValues != null) {
            intValues[index] = (int) value;
        } else {
            longValues[index] = (long) value;
        }
    }

    public Object getValue(int index) {
        if (objectValues != null) {
            return objectValues[index];
        } else if (intValues != null) {
            return intValues[index];
        } else {
            return longValues[index];
        }
    }

    public void removeValue(int index) {
        if (objectValues != null) {
            objectValues[index] = null;
        } else if (intValues != null) {
            intValues[index] = 0;
        } else {
            longValues[index] = 0;
        }
    }

    public void clear() {
        if (objectValues != null) {
            java.util.Arrays.fill(objectValues, null);
        } else if (intValues != null) {
            java.util.Arrays.fill(intValues, 0);
        } else {
            java.util.Arrays.fill(longValues, 0L);
        }
    }
}

public class AccessManager {
    private int[] accessTable;
    private int accessCount;

    public AccessManager(int capacity) {
        this.accessTable = new int[capacity];
    }

    public void updateAccess(int index) {
        accessTable[index] = ++accessCount;
    }

    public void clearAccess(int index) {
        accessTable[index] = 0;
    }

    public void clear() {
        java.util.Arrays.fill(accessTable, 0);
    }
}