package org.kie.dmn.feel.codegen.feel11;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.drools.javaparser.JavaParser;
import org.drools.javaparser.ast.expr.Expression;
import org.drools.javaparser.ast.expr.LambdaExpr;
import org.drools.javaparser.ast.expr.MethodCallExpr;
import org.drools.javaparser.ast.expr.NameExpr;
import org.drools.javaparser.ast.expr.ObjectCreationExpr;
import org.drools.javaparser.ast.expr.StringLiteralExpr;
import org.drools.javaparser.ast.type.ClassOrInterfaceType;
import org.kie.dmn.feel.lang.FunctionDefs;
import org.kie.dmn.feel.lang.ast.BaseNode;
import org.kie.dmn.feel.lang.ast.FunctionDefNode;
import org.kie.dmn.feel.lang.ast.ListNode;
import org.kie.dmn.feel.lang.ast.NameDefNode;
import org.kie.dmn.feel.lang.impl.EvaluationContextImpl;
import org.kie.dmn.feel.lang.impl.FEELEventListenersManager;
import org.kie.dmn.feel.lang.types.BuiltInType;
import org.kie.dmn.feel.parser.feel11.ASTBuilderVisitor;
import org.kie.dmn.feel.parser.feel11.FEEL_1_1Parser;
import org.kie.dmn.feel.util.Msg;

public class Functions {
    public static final ClassOrInterfaceType TYPE_CUSTOM_FEEL_FUNCTION =
            JavaParser.parseClassOrInterfaceType(CompiledCustomFEELFunction.class.getSimpleName());
    private static final Expression ANONYMOUS_STRING_LITERAL = new StringLiteralExpr("<anonymous>");
    private static final Expression EMPTY_LIST = JavaParser.parseExpression("java.util.Collections.emptyList()");

    public static Expression external(List<String> paramNames, BaseNode body) {
        EvaluationContextImpl emptyEvalCtx =
                new EvaluationContextImpl(Functions.class.getClassLoader(), new FEELEventListenersManager());


        Map<String, Object> conf = (Map<String, Object>) body.evaluate(emptyEvalCtx);
        Map<String, String> java = (Map<String, String>) conf.get( "java" );

        if (java != null) {

            String className = java.get("class");
            String methodSignature = java.get("method signature");
            if (className == null || methodSignature == null) {
                throw new FEELCompilationError(Msg.createMessage(Msg.UNABLE_TO_FIND_EXTERNAL_FUNCTION_AS_DEFINED_BY, methodSignature));
            }
            Expression methodCallExpr = FunctionDefs.asMethodCall(className, methodSignature, paramNames);
//            DirectCompilerResult parameters = visit(ctx.formalParameters());

            return methodCallExpr;
        } else {
            throw new FEELCompilationError(Msg.createMessage(Msg.UNABLE_TO_FIND_EXTERNAL_FUNCTION_AS_DEFINED_BY, null));
        }
    }

    public static ObjectCreationExpr internal(Expression parameters, Expression body) {
        ObjectCreationExpr functionDefExpr = new ObjectCreationExpr();
        functionDefExpr.setType(TYPE_CUSTOM_FEEL_FUNCTION);
        functionDefExpr.addArgument(ANONYMOUS_STRING_LITERAL);
        functionDefExpr.addArgument(parameters);
        functionDefExpr.addArgument(body);
        functionDefExpr.addArgument(new MethodCallExpr(new NameExpr("feelExprCtx"), "current"));
        return functionDefExpr;
    }

    public static DirectCompilerResult declaration(FunctionDefNode n, MethodCallExpr list, Expression fnBody) {
        LambdaExpr lambda = Expressions.lambda(fnBody);
        String fnName = Constants.functionName(n.getBody().getText());
        DirectCompilerResult r = DirectCompilerResult.of(
                Functions.internal(list, new NameExpr(fnName)),
                BuiltInType.FUNCTION);
        r.addFieldDesclaration(Constants.function(fnName, lambda));
        return r;
    }




}
