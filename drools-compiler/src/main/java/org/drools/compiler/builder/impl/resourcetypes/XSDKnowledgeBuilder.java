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

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.runtime.pipeline.impl.DroolsJaxbHelperProviderImpl;
import org.drools.core.builder.conf.impl.JaxbConfigurationImpl;
import org.kie.api.io.Resource;

public class XSDKnowledgeBuilder {

    private KnowledgeBuilderImpl knowledgeBuilder;

    public XSDKnowledgeBuilder(KnowledgeBuilderImpl knowledgeBuilder) {

    }

    public void addPackageFromXSD(Resource resource,
                                  JaxbConfigurationImpl configuration) throws IOException {
        if (configuration != null) {
            String[] classes = DroolsJaxbHelperProviderImpl.addXsdModel(
                    resource,
                    knowledgeBuilder,
                    configuration.getXjcOpts(),
                    configuration.getSystemId());
            for (String cls : classes) {
                configuration.getClasses().add(cls);
            }
        }
    }
}
