package org.kie.dmn.feel.codegen.feel11;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.antlr.v4.runtime.tree.ParseTree;
import org.drools.javaparser.ast.CompilationUnit;
import org.drools.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.kie.dmn.feel.lang.CompiledExpression;
import org.kie.dmn.feel.lang.CompilerContext;
import org.kie.dmn.feel.lang.EvaluationContext;
import org.kie.dmn.feel.lang.FEELProfile;
import org.kie.dmn.feel.lang.Type;
import org.kie.dmn.feel.lang.ast.BaseNode;
import org.kie.dmn.feel.lang.impl.CompiledExecutableExpression;
import org.kie.dmn.feel.lang.impl.CompiledExpressionImpl;
import org.kie.dmn.feel.lang.impl.ExecutableExpression;
import org.kie.dmn.feel.lang.impl.FEELEventListenersManager;
import org.kie.dmn.feel.lang.impl.InterpretedExecutableExpression;
import org.kie.dmn.feel.lang.types.BuiltInType;
import org.kie.dmn.feel.parser.feel11.ASTBuilderVisitor;
import org.kie.dmn.feel.parser.feel11.FEELParser;
import org.kie.dmn.feel.parser.feel11.FEEL_1_1Parser;

public class ProcessedExpression {

    private final String packageName;
    private final String className;
    private final String expression;
    private final CompiledFEELSupport.SyntaxErrorListener errorListener =
            new CompiledFEELSupport.SyntaxErrorListener();

    private final BaseNode ast;
    private DirectCompilerResult compiledExpression;
    private final CompilerBytecodeLoader compiler = new CompilerBytecodeLoader();
    public ProcessedExpression(
            String expression,
            FEELEventListenersManager eventsManager,
            CompilerContext ctx,
            List<FEELProfile> profiles) {

        this.expression = expression;
        this.packageName = generateRandomPackage();
        this.className = "TemplateCompiledFEELExpression";
        eventsManager.addListener(errorListener);

        Map<String, Type> variableTypes =
                ctx.getInputVariableTypes();
        FEEL_1_1Parser parser = FEELParser.parse(
                eventsManager,
                expression,
                variableTypes,
                ctx.getInputVariables(),
                ctx.getFEELFunctions(),
                profiles);

        ParseTree tree = parser.compilation_unit();
        ast = tree.accept(new ASTBuilderVisitor(variableTypes));
    }

    private String generateRandomPackage() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return this.getClass().getPackage().getName() + ".gen" + uuid;
    }

    private DirectCompilerResult getCompilerResult() {
        if (compiledExpression == null) {
            if (errorListener.isError()) {
                compiledExpression =
                        DirectCompilerResult.of(
                                CompiledFEELSupport.compiledErrorExpression(
                                        errorListener.event().getMessage()),
                                BuiltInType.UNKNOWN);
            } else {
                try {
                    compiledExpression = ast.accept(new ASTCompilerVisitor());
                } catch (FEELCompilationError e) {
                    compiledExpression = DirectCompilerResult.of(
                            CompiledFEELSupport.compiledErrorExpression(e.getMessage()),
                            BuiltInType.UNKNOWN);
                }
            }
        }
        return compiledExpression;
    }

    public ClassOrInterfaceDeclaration getSourceCode() {
        DirectCompilerResult compilerResult = getCompilerResult();
        CompilationUnit cu = compiler.getCompilationUnitForUnaryTests(
                CompiledFEELUnaryTests.class,
                "/TemplateCompiledFEELExpression.java",
                packageName,
                className,
                expression,
                compilerResult.getExpression(),
                compilerResult.getFieldDeclarations());
        ClassOrInterfaceDeclaration classSource = cu.getClassByName(className).get();
        classSource.setStatic(true);
        return classSource;
    }

    public InterpretedExecutableExpression getInterpreted() {
        return new InterpretedExecutableExpression(new CompiledExpressionImpl(ast));
    }

    public CompiledExecutableExpression getCompiled() {
        DirectCompilerResult compilerResult = getCompilerResult();

        CompiledFEELExpression compiledFEELExpression =
                compiler.makeFromJPExpression(expression,
                                              compilerResult.getExpression(),
                                              compilerResult.getFieldDeclarations());
        return new CompiledExecutableExpression(compiledFEELExpression);
    }
}
