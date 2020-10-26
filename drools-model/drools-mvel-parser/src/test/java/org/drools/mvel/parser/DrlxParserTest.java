package org.drools.mvel.parser;

import java.io.IOException;
import java.nio.file.Paths;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.Test;

import static org.drools.mvel.parser.Providers.provider;
import static org.junit.Assert.assertTrue;

public class DrlxParserTest {

    @Test
    public void testA () throws IOException {
        ParseStart<CompilationUnit> context = ParseStart.COMPILATION_UNIT;
        MvelParser mvelParser = new MvelParser();
        ParseResult<CompilationUnit> parse =
                mvelParser.parse(context,
                                 provider(Paths.get("src/test/resources/org/drools/mvel/parser/Example.drlx")));

        assertTrue(parse.isSuccessful());

    }

}
