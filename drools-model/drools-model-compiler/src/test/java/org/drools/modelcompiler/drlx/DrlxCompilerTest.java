/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.modelcompiler.drlx;

import java.io.IOException;
import java.io.InputStream;

import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import org.drools.compiler.builder.impl.KnowledgeBuilderConfigurationImpl;
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.DialectCompiletimeRegistry;
import org.drools.compiler.compiler.PackageRegistry;
import org.drools.compiler.compiler.ParserError;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.definitions.impl.KnowledgePackageImpl;
import org.drools.core.io.impl.InputStreamResource;
import org.drools.modelcompiler.ExecutableModelProject;
import org.drools.modelcompiler.builder.PackageModel;
import org.drools.modelcompiler.builder.generator.DRLIdGenerator;
import org.drools.modelcompiler.builder.generator.ModelGenerator;
import org.drools.mvel.DrlDumper;
import org.drools.mvel.parser.MvelParser;
import org.drools.mvel.parser.ParseStart;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import static org.drools.mvel.parser.Providers.provider;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DrlxCompilerTest {

    @Test
    public void testSingleFileUnit() throws Exception {
        InputStream p = getClass().getClassLoader().getResourceAsStream("drlx1/Example.drlx");
        InputStreamResource r = new InputStreamResource(p);

        ParseStart<CompilationUnit> context = ParseStart.DRLX_COMPILATION_UNIT;
        MvelParser mvelParser = new MvelParser();
        ParseResult<CompilationUnit> result =
                mvelParser.parse(context,
                                 provider(r.getReader()));
        if (result.isSuccessful()) {
            org.drools.compiler.drlx.DrlxCompiler drlxCompiler = new org.drools.compiler.drlx.DrlxCompiler();
            PackageDescr pkg = drlxCompiler.visit(result.getResult().get(), null);
            System.out.println(new DrlDumper().dump(pkg));
        } else {
            for (Problem problem : result.getProblems()) {
                TokenRange tokenRange = problem.getLocation().get();
                Range range = tokenRange.getBegin().getRange().get();
                int lineCount = range.getLineCount();
                System.out.println(new ParserError(problem.getMessage(), lineCount, -1));
                fail();
            }
        }
    }

    @Test
    public void testCompileUnit() throws IOException {
        InputStream p = getClass().getClassLoader().getResourceAsStream("drlx1/Example.drlx");
        InputStreamResource r = new InputStreamResource(p);

        ParseStart<CompilationUnit> context = ParseStart.DRLX_COMPILATION_UNIT;
        MvelParser mvelParser = new MvelParser();
        ParseResult<CompilationUnit> result =
                mvelParser.parse(context,
                                 provider(r.getReader()));
        org.drools.compiler.drlx.DrlxCompiler drlxCompiler = new org.drools.compiler.drlx.DrlxCompiler();
        PackageDescr packageDescr = drlxCompiler.visit(result.getResult().get(), null);

        ModelGenerator modelGenerator = new ModelGenerator();

        KnowledgeBuilderImpl kbuilder = new KnowledgeBuilderImpl();
        PackageRegistry registry = kbuilder.getOrCreatePackageRegistry(packageDescr);
        kbuilder.getAndRegisterTypeDeclaration(org.drools.modelcompiler.drlx.Example.class, "org.drools.modelcompiler.drlx");
        InternalKnowledgePackage knowledgePackage = registry.getPackage();
        PackageModel packageModel = new PackageModel(new ReleaseIdImpl("", "", ""), packageDescr.getName(), new KnowledgeBuilderConfigurationImpl(), false, new DialectCompiletimeRegistry(), new DRLIdGenerator());
        modelGenerator.generateModel(
                kbuilder,
                knowledgePackage,
                packageDescr,
                packageModel,
                false);

        assertEquals(packageModel.getRuleUnits().size(), 1);


    }

    private static String getPom(ReleaseId releaseId) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "  <groupId>" + releaseId.getGroupId() + "</groupId>\n" +
                "  <artifactId>" + releaseId.getArtifactId() + "</artifactId>\n" +
                "  <version>" + releaseId.getVersion() + "</version>\n" +
                "</project>\n";
    }
}
