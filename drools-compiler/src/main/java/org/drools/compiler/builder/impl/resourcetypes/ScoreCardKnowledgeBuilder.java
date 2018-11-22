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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.drools.compiler.builder.impl.KnowledgeBuilderConfigurationImpl;
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.DrlParser;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.compiler.GuidedDecisionTableFactory;
import org.drools.compiler.compiler.GuidedDecisionTableProvider;
import org.drools.compiler.compiler.GuidedScoreCardFactory;
import org.drools.compiler.compiler.ParserError;
import org.drools.compiler.compiler.ResourceConversionResult;
import org.drools.compiler.compiler.ScoreCardFactory;
import org.drools.compiler.lang.descr.PackageDescr;
import org.kie.api.internal.assembler.KieAssemblerService;
import org.kie.api.internal.assembler.KieAssemblers;
import org.kie.api.internal.utils.ServiceRegistry;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;
import org.kie.api.io.ResourceWithConfiguration;
import org.kie.internal.builder.ScoreCardConfiguration;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.io.ResourceWithConfigurationImpl;

public class ScoreCardKnowledgeBuilder {

    private DSLRKnowledgeBuilder dslrPackage;
    private KnowledgeBuilderImpl knowledgeBuilder;
    private KnowledgeBuilderConfigurationImpl configuration;

    public ScoreCardKnowledgeBuilder(KnowledgeBuilderImpl knowledgeBuilder) {

    }
    public void addPackageFromGuidedScoreCard(final Resource resource) throws DroolsParserException, IOException {
        this.resource = resource;
        final String pmmlString = GuidedScoreCardFactory.getPMMLStringFromInputStream(resource.getInputStream());
        if (pmmlString != null) {
            addPackageFromScoreCard(pmmlString, "guided_scorecard_generated.pmml");
        }
        this.resource = null;
    }

    public void addPackageFromScoreCard(final Resource resource,
                                        final ResourceConfiguration configuration) throws DroolsParserException, IOException {
        this.resource = resource;
        final ScoreCardConfiguration scardConfiguration = configuration instanceof ScoreCardConfiguration ?
                (ScoreCardConfiguration) configuration :
                null;
        final String pmmlString = ScoreCardFactory.getPMMLStringFromInputStream(resource.getInputStream(), scardConfiguration);
        if (pmmlString != null) {
            addPackageFromScoreCard(pmmlString, "scorecard_generated.pmml");
        }
        this.resource = null;
    }
    private void addPackageFromScoreCard(final String pmmlString, final String fileName) throws DroolsParserException, IOException  {
        final File dumpDir = this.configuration.getDumpDir();
        if (dumpDir != null) {
            final String dirName = dumpDir.getCanonicalPath().endsWith("/") ? dumpDir.getCanonicalPath() : dumpDir.getCanonicalPath() + "/";
            final String outputPath = dirName + fileName;
            try (final FileOutputStream fos = new FileOutputStream(outputPath)) {
                fos.write(pmmlString.getBytes());
            }
        }
        final Resource res = ResourceFactory.newByteArrayResource(pmmlString.getBytes());

        try {
            ResourceWithConfiguration resCon = new ResourceWithConfigurationImpl(res, null, null, null);
            addPackageForExternalType(ResourceType.PMML, Arrays.asList(resCon));
        } catch (Exception e) {
            throw new DroolsParserException(e);
        }
    }

    void addPackageForExternalType(ResourceType type, List<ResourceWithConfiguration> resources) throws Exception {
        KieAssemblers assemblers = ServiceRegistry.getInstance().get(KieAssemblers.class);

        KieAssemblerService assembler = assemblers.getAssemblers().get(type);

        if (assembler != null) {
            assembler.addResources(this, resources, type);
        } else {
            throw new RuntimeException("Unknown resource type: " + type);
        }
    }


}
