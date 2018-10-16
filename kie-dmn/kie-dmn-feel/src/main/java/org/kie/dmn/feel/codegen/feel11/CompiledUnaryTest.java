package org.kie.dmn.feel.codegen.feel11;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.drools.javaparser.ast.CompilationUnit;
import org.drools.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.kie.dmn.feel.lang.CompilerContext;
import org.kie.dmn.feel.lang.FEELProfile;
import org.kie.dmn.feel.lang.Type;
import org.kie.dmn.feel.lang.ast.BaseNode;
import org.kie.dmn.feel.lang.impl.CompiledExpressionImpl;
import org.kie.dmn.feel.lang.impl.FEELEventListenersManager;
import org.kie.dmn.feel.lang.impl.UnaryTestCompiledExecutableExpression;
import org.kie.dmn.feel.lang.impl.UnaryTestInterpretedExecutableExpression;
import org.kie.dmn.feel.parser.feel11.ASTBuilderVisitor;
import org.kie.dmn.feel.parser.feel11.FEELParser;
import org.kie.dmn.feel.parser.feel11.FEEL_1_1Parser;

public class CompiledUnaryTest {

    private final String packageName;
    private final String className;
    private final String input;

    private final CompiledFEELSupport.SyntaxErrorListener errorListener =
            new CompiledFEELSupport.SyntaxErrorListener();
    private final BaseNode ast;
    private DirectCompilerResult compiledExpression;
    private final CompilerBytecodeLoader compiler =
            new CompilerBytecodeLoader();

    public CompiledUnaryTest(
            FEELEventListenersManager eventsManager,
            String input,
            String packageName,
            String testClass,
            CompilerContext ctx,
            List<FEELProfile> profiles) {
        this.input = input;
        this.packageName = packageName;
        this.className = testClass;
        eventsManager.addListener(errorListener);

        Map<String, Type> variableTypes =
                ctx.getInputVariableTypes();
        FEEL_1_1Parser parser = FEELParser.parse(
                eventsManager,
                input,
                variableTypes,
                ctx.getInputVariables(),
                ctx.getFEELFunctions(),
                profiles);

        ParseTree tree = parser.unaryTestsRoot();
        BaseNode initialAst = tree.accept(new ASTBuilderVisitor(ctx.getInputVariableTypes()));
        ast = initialAst.accept(new ASTUnaryTestTransform()).node();
    }

    public CompiledUnaryTest(
            FEELEventListenersManager eventsManager,
            String expressions,
            String generateRandomPackage,
            CompilerContext ctx) {
        this(eventsManager,
             expressions,
             generateRandomPackage,
             "TemplateCompiledFEELUnaryTests",
             ctx,
             Collections.emptyList());
    }

    private DirectCompilerResult getCompilerResult() {
        if (compiledExpression == null) {
            if (errorListener.isError()) {
                compiledExpression = CompiledFEELSupport.compiledErrorUnaryTest(errorListener.event().getMessage());
            } else {
                try {
                    compiledExpression = ast.accept(new ASTCompilerVisitor());
                } catch (FEELCompilationError e) {
                    compiledExpression = CompiledFEELSupport.compiledErrorUnaryTest(e.getMessage());
                }
            }
        }
        return compiledExpression;
    }

    public ClassOrInterfaceDeclaration getSourceCode() {
        DirectCompilerResult compilerResult = getCompilerResult();
        CompilationUnit cu = compiler.getCompilationUnitForUnaryTests(
                CompiledFEELUnaryTests.class,
                "/TemplateCompiledFEELUnaryTests.java",
                packageName,
                className,
                input,
                compilerResult.getExpression(),
                compilerResult.getFieldDeclarations());
        ClassOrInterfaceDeclaration classSource = cu.getClassByName(className).get();
        classSource.setStatic(true);
        return classSource;
    }

    public UnaryTestInterpretedExecutableExpression getInterpreted() {
        if (errorListener.isError()) {
            return UnaryTestInterpretedExecutableExpression.EMPTY;
        } else {
            return new UnaryTestInterpretedExecutableExpression(new CompiledExpressionImpl(ast));
        }
    }

    public UnaryTestCompiledExecutableExpression getCompiled() {
        DirectCompilerResult compilerResult = getCompilerResult();

        CompiledFEELUnaryTests compiledFEELExpression =
                compiler.makeFromJPUnaryTestsExpression(input,
                                                        compilerResult.getExpression(),
                                                        compilerResult.getFieldDeclarations());
        return new UnaryTestCompiledExecutableExpression(compiledFEELExpression);
    }
}
