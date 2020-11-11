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

import java.io.InputStream;

import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import org.drools.compiler.compiler.ParserError;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.core.io.impl.InputStreamResource;
import org.drools.mvel.DrlDumper;
import org.drools.mvel.parser.MvelParser;
import org.drools.mvel.parser.ParseStart;
import org.junit.Test;

import static org.drools.mvel.parser.Providers.provider;
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

}
