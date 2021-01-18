package org.drools.modelcompiler.drlx;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.lang.descr.PackageDescr;
import org.kie.api.internal.assembler.KieAssemblerService;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;

public class DrlxAssembler implements KieAssemblerService {

    @Override
    public ResourceType getResourceType() {
        return ResourceType.DRLX;
    }

    @Override
    public void addResource(Object kbuilder, Resource resource, ResourceType type, ResourceConfiguration configuration) throws Exception {
        KnowledgeBuilderImpl kieBuilder = (KnowledgeBuilderImpl) kbuilder;
        DrlxCompiler drlxCompiler = new DrlxCompiler();
        PackageDescr packageDescr = drlxCompiler.toPackageDescr(resource);
        kieBuilder.addPackage(packageDescr);
    }


}
