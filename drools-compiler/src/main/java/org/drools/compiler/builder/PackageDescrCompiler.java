package org.drools.compiler.builder;

import java.io.IOException;
import java.util.List;

import org.drools.compiler.lang.descr.PackageDescr;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;
import org.kie.internal.builder.KnowledgeBuilderResult;

public interface PackageDescrCompiler {

    ResourceType getResourceType();

    PackageDescr toPackageDescr(Resource resource, ResourceConfiguration configuration) throws IOException;

    List<KnowledgeBuilderResult> getResults();
}
