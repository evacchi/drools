package org.kie.api2.impl;

import org.drools.core.datasources.CursoredDataSource;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api2.api.DataSource;

public class DataSourceImpl<T> implements DataSource<T> {

    private final CursoredDataSource<T> cds = new CursoredDataSource<>();

    @Override
    public FactHandle add(T object) {
        return cds.insert(object);
    }

    @Override
    public void update(FactHandle handle, T object) {
        cds.update(handle, object);
    }

    @Override
    public void remove(FactHandle handle) {
        cds.delete(handle);
    }

}
