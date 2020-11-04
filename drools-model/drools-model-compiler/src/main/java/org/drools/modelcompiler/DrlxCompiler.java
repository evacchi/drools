package org.drools.modelcompiler;

import org.drools.compiler.lang.api.DescrFactory;
import org.drools.compiler.lang.api.PackageDescrBuilder;
import org.drools.compiler.lang.descr.PackageDescr;
import org.kie.api.io.Resource;

public class DrlxCompiler {
    public PackageDescr compile(Resource resource) {
        PackageDescrBuilder packageDescrBuilder = DescrFactory.newPackage(resource);
        return packageDescrBuilder.getDescr();
    }
}
