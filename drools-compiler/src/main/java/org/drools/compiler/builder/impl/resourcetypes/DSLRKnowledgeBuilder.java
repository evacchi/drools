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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.drools.compiler.builder.impl.BuilderResultCollector;
import org.drools.compiler.builder.impl.KnowledgeBuilderConfigurationImpl;
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.DrlParser;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.lang.ExpanderException;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.compiler.lang.dsl.DSLMappingFile;
import org.drools.compiler.lang.dsl.DSLTokenizedMappingFile;
import org.drools.compiler.lang.dsl.DefaultExpander;
import org.kie.api.io.Resource;

public class DSLRKnowledgeBuilder {

    private final KnowledgeBuilderImpl knowledgeBuilder;
    KnowledgeBuilderConfigurationImpl configuration;
    protected final BuilderResultCollector results;
    private List<DSLTokenizedMappingFile> dslFiles;

    public DSLRKnowledgeBuilder(KnowledgeBuilderImpl knowledgeBuilder) {
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
