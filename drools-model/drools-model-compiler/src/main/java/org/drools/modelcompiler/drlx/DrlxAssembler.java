package org.drools.modelcompiler.drlx;

import org.drools.compiler.builder.impl.CompositeKnowledgeBuilderImpl;
import org.drools.compiler.lang.descr.PackageDescr;
import org.kie.api.internal.assembler.KieAssemblerService;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrlxAssembler implements KieAssemblerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrlxAssembler.class);

    @Override
    public ResourceType getResourceType() {
        return ResourceType.DRLX;
    }

    @Override
    public void addResourceAsPackageDescr(Object kbuilder, Resource resource, ResourceType type, ResourceConfiguration configuration) throws Exception {
        if (!(kbuilder instanceof CompositeKnowledgeBuilderImpl)) {
            LOGGER.warn("DrlxAssembler is support only for CompositeKnowledgeBuilderImpl. Skipping: {} given", kbuilder.getClass());
            return;
        }
        CompositeKnowledgeBuilderImpl kieBuilder = (CompositeKnowledgeBuilderImpl) kbuilder;
        DrlxCompiler drlxCompiler = new DrlxCompiler();
        PackageDescr packageDescr = drlxCompiler.toPackageDescr(resource);
        kieBuilder.addPackageDescr(resource, packageDescr);
    }

    @Override
    public void addResource(Object kbuilder, Resource resource, ResourceType type, ResourceConfiguration configuration) throws Exception {

    }
}
