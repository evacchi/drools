package org.drools.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.ObjectStore;
import org.kie.api.runtime.rule.FactHandle;

abstract class AbstractImmutableCollection
        implements
        Collection {

    public boolean add(Object o) {
        throw new UnsupportedOperationException("This is an immmutable Collection");
    }

    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException("This is an immmutable Collection");
    }

    public void clear() {
        throw new UnsupportedOperationException("This is an immmutable Collection");
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException("This is an immmutable Collection");
    }

    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException("This is an immmutable Collection");
    }

    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException("This is an immmutable Collection");
    }
}

class MyObjectStoreWrapper extends AbstractImmutableCollection {

    public ObjectStore store;
    public org.kie.api.runtime.ObjectFilter filter;
    public int type;           // 0 == object, 1 == facthandle
    public static final int OBJECT = 0;
    public static final int FACT_HANDLE = 1;

    public MyObjectStoreWrapper(ObjectStore store,
                                org.kie.api.runtime.ObjectFilter filter,
                                int type) {
        this.store = store;
        this.filter = filter;
        this.type = type;
    }

    public boolean contains(Object object) {
        if (object instanceof FactHandle) {
            return this.store.getObjectForHandle((InternalFactHandle) object) != null;
        } else {
            return this.store.getHandleForObject(object) != null;
        }
    }

    public boolean containsAll(Collection c) {
        for (Object object : c) {
            if (!contains(object)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        if (this.filter == null) {
            return this.store.isEmpty();
        }

        return size() == 0;
    }

    public int size() {
        if (this.filter == null) {
            return this.store.size();
        }

        int i = 0;
        for (Object o : this) {
            i++;
        }

        return i;
    }

    public Iterator<?> iterator() {
        Iterator it;
        if (type == OBJECT) {
            if (filter != null) {
                it = store.iterateObjects(filter);
            } else {
                it = store.iterateObjects();
            }
        } else {
            if (filter != null) {
                it = store.iterateFactHandles(filter);
            } else {
                it = store.iterateFactHandles();
            }
        }
        return it;
    }

    public Object[] toArray() {
        return asList().toArray();
    }

    public Object[] toArray(Object[] array) {
        return asList().toArray(array);
    }

    private List asList() {
        List list = new ArrayList();
        for (Object o : this) {
            list.add(o);
        }
        return list;
    }
}
