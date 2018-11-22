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

package org.drools.compiler.builder.impl.resourcetypes;

import java.io.IOException;
import java.io.Reader;

import org.drools.compiler.builder.impl.KnowledgeBuilderConfigurationImpl;
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.core.io.impl.ClassPathResource;
import org.drools.core.io.internal.InternalResource;
import org.drools.core.xml.XmlChangeSetReader;
import org.kie.api.io.Resource;
import org.kie.internal.ChangeSet;
import org.xml.sax.SAXException;

public class ChangeSetKnowledgeBuilder {

    private KnowledgeBuilderImpl knowledgeBuilder;
    private KnowledgeBuilderConfigurationImpl configuration;

    public void addPackageFromChangeSet(Resource resource) throws SAXException,
            IOException {
        XmlChangeSetReader reader = new XmlChangeSetReader(this.configuration.getSemanticModules());
        if (resource instanceof ClassPathResource) {
            reader.setClassLoader(((ClassPathResource) resource).getClassLoader(),
                                  ((ClassPathResource) resource).getClazz());
        } else {
            reader.setClassLoader(this.configuration.getClassLoader(),
                                  null);
        }
        Reader resourceReader = null;
        try {
            resourceReader = resource.getReader();
            ChangeSet changeSet = reader.read(resourceReader);
            if (changeSet == null) {
                // @TODO should log an error
            }
            for (Resource nestedResource : changeSet.getResourcesAdded()) {
                InternalResource iNestedResourceResource = (InternalResource) nestedResource;
                if (iNestedResourceResource.isDirectory()) {
                    for (Resource childResource : iNestedResourceResource.listResources()) {
                        if (((InternalResource) childResource).isDirectory()) {
                            continue; // ignore sub directories
                        }
                        ((InternalResource) childResource).setResourceType(iNestedResourceResource.getResourceType());
                        knowledgeBuilder.addKnowledgeResource(childResource,
                                                              iNestedResourceResource.getResourceType(),
                                                              iNestedResourceResource.getConfiguration());
                    }
                } else {
                    knowledgeBuilder.addKnowledgeResource(iNestedResourceResource,
                                                          iNestedResourceResource.getResourceType(),
                                                          iNestedResourceResource.getConfiguration());
                }
            }
        } finally {
            if (resourceReader != null) {
                resourceReader.close();
            }
        }
    }
}
