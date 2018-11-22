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
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.drools.compiler.builder.impl.BuilderResultCollector;
import org.drools.compiler.builder.impl.KnowledgeBuilderConfigurationImpl;
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.DecisionTableFactory;
import org.drools.compiler.compiler.DrlParser;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.compiler.ParserError;
import org.drools.compiler.lang.ExpanderException;
import org.drools.compiler.lang.descr.CompositePackageDescr;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.compiler.lang.dsl.DSLMappingFile;
import org.drools.compiler.lang.dsl.DSLTokenizedMappingFile;
import org.drools.compiler.lang.dsl.DefaultExpander;
import org.drools.core.util.IoUtils;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.internal.builder.DecisionTableConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.drools.compiler.builder.impl.resourcetypes.DRLKnowledgeBuilder.createDumpDrlFile;

public class DTABLEKnowledgeBuilder {
    protected static final transient Logger logger = LoggerFactory.getLogger(DRLKnowledgeBuilder.class);

    private final KnowledgeBuilderImpl knowledgeBuilder;
    KnowledgeBuilderConfigurationImpl configuration;
    protected final BuilderResultCollector results;
    private List<DSLTokenizedMappingFile> dslFiles;

    public DTABLEKnowledgeBuilder(KnowledgeBuilderImpl knowledgeBuilder) {
        this.knowledgeBuilder = knowledgeBuilder;
        configuration = knowledgeBuilder.getBuilderConfiguration();
        dslFiles = new ArrayList<>();
        results = new BuilderResultCollector();
    }

    public PackageDescr generatedDslrToPackageDescr(Resource resource, String dslr) throws DroolsParserException {
        return dslrReaderToPackageDescr(resource, new StringReader(dslr));
    }

    public PackageDescr dslrToPackageDescr(Resource resource) throws DroolsParserException,
            IOException {
        return dslrReaderToPackageDescr(resource, resource.getReader());
    }


    public PackageDescr decisionTableToPackageDescr(Resource resource,
                                                    ResourceConfiguration configuration) throws DroolsParserException,
            IOException {
        DecisionTableConfiguration dtableConfiguration = configuration instanceof DecisionTableConfiguration ?
                (DecisionTableConfiguration) configuration :
                null;

        if (dtableConfiguration != null && !dtableConfiguration.getRuleTemplateConfigurations().isEmpty()) {
            List<String> generatedDrls = DecisionTableFactory.loadFromInputStreamWithTemplates(resource, dtableConfiguration);
            if (generatedDrls.size() == 1) {
                return generatedDrlToPackageDescr(resource, generatedDrls.get(0));
            }
            CompositePackageDescr compositePackageDescr = null;
            for (String generatedDrl : generatedDrls) {
                PackageDescr packageDescr = generatedDrlToPackageDescr(resource, generatedDrl);
                if (packageDescr != null) {
                    if (compositePackageDescr == null) {
                        compositePackageDescr = new CompositePackageDescr(resource, packageDescr);
                    } else {
                        compositePackageDescr.addPackageDescr(resource, packageDescr);
                    }
                }
            }
            return compositePackageDescr;
        }

        String generatedDrl = DecisionTableFactory.loadFromResource(resource, dtableConfiguration);
        return generatedDrlToPackageDescr(resource, generatedDrl);
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

    /**
     * Returns an expander for DSLs (only if there is a DSL configured for this
     * package).
     */
    public DefaultExpander getDslExpander() {
        DefaultExpander expander = new DefaultExpander();
        if (this.dslFiles == null || this.dslFiles.isEmpty()) {
            return null;
        }
        for (DSLMappingFile file : this.dslFiles) {
            expander.addDSLMapping(file.getMapping());
        }
        return expander;
    }
    public void addDsl(Resource resource) throws IOException {
        DSLTokenizedMappingFile file = new DSLTokenizedMappingFile();

        Reader reader = null;
        try {
            reader = resource.getReader();
            if (!file.parseAndLoad(reader)) {
                this.results.addAll(file.getErrors());
            }
            if (this.dslFiles == null) {
                this.dslFiles = new ArrayList<DSLTokenizedMappingFile>();
            }
            this.dslFiles.add(file);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public PackageDescr dslrReaderToPackageDescr(Resource resource, Reader dslrReader) throws DroolsParserException {
        boolean hasErrors;
        PackageDescr pkg;

        DrlParser parser = new DrlParser(configuration.getLanguageLevel());
        DefaultExpander expander = getDslExpander();

        try {
            if (expander == null) {
                expander = new DefaultExpander();
            }
            String str = expander.expand(dslrReader);
            if (expander.hasErrors()) {
                for (ExpanderException error : expander.getErrors()) {
                    error.setResource(resource);
                    results.add(error);
                }
            }

            pkg = parser.parse(resource, str);
            this.results.addAll(parser.getErrors());
            hasErrors = parser.hasErrors();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (dslrReader != null) {
                try {
                    dslrReader.close();
                } catch (IOException e) {
                }
            }
        }
        return hasErrors ? null : pkg;
    }

}
