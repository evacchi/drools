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
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.UUID;

import org.drools.compiler.builder.impl.BuilderResultCollector;
import org.drools.compiler.builder.impl.KnowledgeBuilderConfigurationImpl;
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.DrlParser;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.compiler.GuidedDecisionTableFactory;
import org.drools.compiler.compiler.GuidedDecisionTableProvider;
import org.drools.compiler.compiler.ParserError;
import org.drools.compiler.compiler.ResourceConversionResult;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.core.util.IoUtils;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.drools.compiler.builder.impl.resourcetypes.DRLKnowledgeBuilder.createDumpDrlFile;

public class GDSTKnowledgeBuilder {
    protected static final transient Logger logger = LoggerFactory.getLogger(DRLKnowledgeBuilder.class);

    private DSLRKnowledgeBuilder dslrPackage;
    private KnowledgeBuilderConfigurationImpl configuration;
    private BuilderResultCollector results;

    public GDSTKnowledgeBuilder(KnowledgeBuilderImpl knowledgeBuilder) {

    }

    public PackageDescr guidedDecisionTableToPackageDescr(Resource resource) throws DroolsParserException,
            IOException {
        GuidedDecisionTableProvider guidedDecisionTableProvider = GuidedDecisionTableFactory.getGuidedDecisionTableProvider();
        ResourceConversionResult conversionResult = guidedDecisionTableProvider.loadFromInputStream(resource.getInputStream());
        return conversionResultToPackageDescr(resource, conversionResult);
    }

    private PackageDescr conversionResultToPackageDescr(Resource resource, ResourceConversionResult resourceConversionResult)
            throws DroolsParserException {
        ResourceType resourceType = resourceConversionResult.getType();
        if (ResourceType.DSLR.equals(resourceType)) {
            return dslrPackage.generatedDslrToPackageDescr(resource, resourceConversionResult.getContent());
        } else if (ResourceType.DRL.equals(resourceType)) {
            return generatedDrlToPackageDescr(resource, resourceConversionResult.getContent());
        } else {
            throw new RuntimeException("Converting generated " + resourceType + " into PackageDescr is not supported!");
        }
    }

    private PackageDescr generatedDrlToPackageDescr(Resource resource, String generatedDrl) throws DroolsParserException {
        // dump the generated DRL if the dump dir was configured
        if (this.configuration.getDumpDir() != null) {
            dumpDrlGeneratedFromDTable(this.configuration.getDumpDir(), generatedDrl, resource.getSourcePath());
        }

        DrlParser parser = new DrlParser(configuration.getLanguageLevel());
        PackageDescr pkg = parser.parse(resource, new StringReader(generatedDrl));
        this.results.addAll(parser.getErrors());
        if (pkg == null) {
            results.add(new ParserError(resource, "Parser returned a null Package", 0, 0));
        } else {
            pkg.setResource(resource);
        }
        return parser.hasErrors() ? null : pkg;
    }
    private void dumpDrlGeneratedFromDTable(File dumpDir, String generatedDrl, String srcPath) {
        File dumpFile;
        if (srcPath != null) {
            dumpFile = createDumpDrlFile(dumpDir, srcPath, ".drl");
        } else {
            dumpFile = createDumpDrlFile(dumpDir, "decision-table-" + UUID.randomUUID(), ".drl");
        }
        try {
            IoUtils.write(dumpFile, generatedDrl.getBytes(IoUtils.UTF8_CHARSET));
        } catch (IOException ex) {
            // nothing serious, just failure when writing the generated DRL to file, just log the exception and continue
            logger.warn("Can't write the DRL generated from decision table to file " + dumpFile.getAbsolutePath() + "!\n" +
                                Arrays.toString(ex.getStackTrace()));
        }
    }

}
