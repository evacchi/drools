/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.compiler.builder;

import java.util.Map;

import org.drools.compiler.compiler.PackageRegistry;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.kie.api.internal.assembler.KieAssemblerService;
import org.kie.api.internal.io.ResourceTypePackage;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;

public abstract class AbstractAssemblerService<T extends ResourceTypePackage<U>, U extends CompiledResource> implements KieAssemblerService {

    protected abstract T createPackage(String namespace);
    protected abstract U compile(AssemblerContext ctx, Resource resource);

    @Override
    public final void addResource(Object kbuilder, Resource resource, ResourceType type, ResourceConfiguration configuration) throws Exception {

        AssemblerContext kb = (AssemblerContext) kbuilder;

        U compiled = compile(kb, resource);

        PackageRegistry pkgReg = kb.getOrCreatePackageRegistry(new PackageDescr(compiled.getNamespace() ) );

        InternalKnowledgePackage kpkgs = pkgReg.getPackage();

        Map<ResourceType, ResourceTypePackage> rpkg = kpkgs.getResourceTypePackages();
        T bpkg = (T) rpkg.get(getResourceType());
        if ( bpkg == null ) {
            bpkg = createPackage(compiled.getNamespace());
            rpkg.put(getResourceType(), bpkg);
        }
        bpkg.add(compiled);
    }
}
