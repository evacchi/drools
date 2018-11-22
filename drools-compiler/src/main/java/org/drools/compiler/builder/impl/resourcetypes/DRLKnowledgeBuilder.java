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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.drools.compiler.builder.impl.BuilderResultCollector;
import org.drools.compiler.builder.impl.KnowledgeBuilderConfigurationImpl;
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.BaseKnowledgeBuilderResultImpl;
import org.drools.compiler.compiler.DrlParser;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.compiler.ParserError;
import org.drools.compiler.compiler.ProcessBuilderFactory;
import org.drools.compiler.compiler.ProcessLoadError;
import org.drools.compiler.compiler.xml.XmlPackageReader;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.core.io.impl.DescrResource;
import org.drools.core.io.impl.ReaderResource;
import org.drools.core.util.IoUtils;
import org.kie.api.definition.process.Process;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderResult;
import org.kie.internal.builder.ResourceChange;
import org.kie.internal.builder.conf.LanguageLevelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class DRLKnowledgeBuilder {
    protected static final transient Logger logger = LoggerFactory.getLogger(DRLKnowledgeBuilder.class);

    private final org.drools.compiler.compiler.ProcessBuilder processBuilder;
    protected final KnowledgeBuilderImpl knowledgeBuilder;
    protected final BuilderResultCollector results;
    private KnowledgeBuilderImpl.AssetFilter assetFilter; // fixme
    private List<Process> processes;
    private KnowledgeBuilderConfigurationImpl configuration;

    public DRLKnowledgeBuilder(
            KnowledgeBuilderImpl knowledgeBuilder) {
        this.processBuilder = ProcessBuilderFactory.newProcessBuilder(knowledgeBuilder);
        this.knowledgeBuilder = knowledgeBuilder;
        configuration = knowledgeBuilder.getBuilderConfiguration();
        this.results = new BuilderResultCollector();
        this.processes = Collections.emptyList();
    }

    public PackageDescr drlToPackageDescr(Resource resource) throws DroolsParserException,
            IOException {
        PackageDescr pkg;
        boolean hasErrors = false;
        if (resource instanceof DescrResource) {
            pkg = (PackageDescr) ((DescrResource) resource).getDescr();
        } else {
            final DrlParser parser = new DrlParser(knowledgeBuilder.getBuilderConfiguration().getLanguageLevel());
            pkg = parser.parse(resource);
            this.results.addAll(parser.getErrors());
            if (pkg == null) {
                this.results.add(new ParserError(resource, "Parser returned a null Package", 0, 0));
            }
            hasErrors = parser.hasErrors();
        }
        if (pkg != null) {
            pkg.setResource(resource);
        }
        return hasErrors ? null : pkg;
    }

    public PackageDescr xmlToPackageDescr(Resource resource) throws DroolsParserException,
            IOException {
        final XmlPackageReader xmlReader = new XmlPackageReader(knowledgeBuilder.getBuilderConfiguration().getSemanticModules());
        xmlReader.getParser().setClassLoader(knowledgeBuilder.getRootClassLoader());

        Reader reader = null;
        try {
            reader = resource.getReader();
            xmlReader.read(reader);
        } catch (final SAXException e) {
            throw new DroolsParserException(e.toString(),
                                            e.getCause());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return xmlReader.getPackageDescr();
    }



    public static File createDumpDrlFile(File dumpDir, String fileName, String extension) {
        return new File(dumpDir, fileName.replaceAll("[^a-zA-Z0-9\\.\\-_]+", "_") + extension);
    }

}
