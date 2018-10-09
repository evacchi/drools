package org.kie.dmn.feel.codegen.feel11;

import java.util.List;

import org.drools.javaparser.JavaParser;
import org.drools.javaparser.ast.NodeList;
import org.drools.javaparser.ast.body.FieldDeclaration;
import org.drools.javaparser.ast.body.Parameter;
import org.drools.javaparser.ast.expr.CastExpr;
import org.drools.javaparser.ast.expr.ClassExpr;
import org.drools.javaparser.ast.expr.EnclosedExpr;
import org.drools.javaparser.ast.expr.Expression;
import org.drools.javaparser.ast.expr.LambdaExpr;
import org.drools.javaparser.ast.expr.MethodCallExpr;
import org.drools.javaparser.ast.expr.NameExpr;
import org.drools.javaparser.ast.expr.ObjectCreationExpr;
import org.drools.javaparser.ast.expr.StringLiteralExpr;
import org.drools.javaparser.ast.stmt.ExpressionStmt;
import org.drools.javaparser.ast.type.ClassOrInterfaceType;
import org.drools.javaparser.ast.type.Type;
import org.drools.javaparser.ast.type.UnknownType;
import org.kie.dmn.feel.lang.ast.InfixOpNode;
import org.kie.dmn.feel.lang.ast.QuantifiedExpressionNode;
import org.kie.dmn.feel.lang.ast.RangeNode;
import org.kie.dmn.feel.lang.ast.UnaryTestNode;
import org.kie.dmn.feel.lang.impl.NamedParameter;

import static org.kie.dmn.feel.codegen.feel11.Constants.BigDecimalT;
import static org.kie.dmn.feel.codegen.feel11.Constants.BuiltInTypeT;
import static org.kie.dmn.feel.codegen.feel11.Constants.DECIMAL_128;

public class Expressions {

    public static final ClassOrInterfaceType NamedParamterT = new ClassOrInterfaceType(null, NamedParameter.class.getCanonicalName());

    public static class NamedLambda {

        private final NameExpr name;
        private final LambdaExpr expr;
        private final FieldDeclaration field;

        private NamedLambda(NameExpr name, LambdaExpr expr, FieldDeclaration field) {
            this.name = name;
            this.expr = expr;
            this.field = field;
        }

        public NameExpr name() {
            return name;
        }

        public LambdaExpr expr() {
            return expr;
        }

        public FieldDeclaration field() {
            return field;
        }
    }

    private static final Expression QUANTIFIER_SOME = JavaParser.parseExpression("org.kie.dmn.feel.lang.ast.QuantifiedExpressionNode.Quantifier.SOME");
    private static final Expression QUANTIFIER_EVERY = JavaParser.parseExpression("org.kie.dmn.feel.lang.ast.QuantifiedExpressionNode.Quantifier.EVERY");

    public static final String LEFT = "left";
    public static final NameExpr LEFT_EXPR = new NameExpr(LEFT);
    public static final UnknownType UNKNOWN_TYPE = new UnknownType();
    public static final NameExpr STDLIB = new NameExpr(CompiledFEELSupport.class.getSimpleName());

    public static Expression negate(Expression expression) {
        EnclosedExpr e = castTo(BigDecimalT, expression);
        return new MethodCallExpr(e, "negate");
    }

    public static MethodCallExpr binary(
            InfixOpNode.InfixOperator operator,
            Expression l,
            Expression r) {
        switch (operator) {
            case ADD:
                return arithmetic("add", l, r);
            case SUB:
                return arithmetic("sub", l, r);
            case MULT:
                return arithmetic("mult", l, r);
            case DIV:
                return arithmetic("div", l, r);
            case POW:
                return arithmetic("pow", l, r);

            case LTE:
                return comparison("lte", l, r);
            case LT:
                return comparison("lt", l, r);
            case GT:
                return comparison("gt", l, r);
            case GTE:
                return comparison("gte", l, r);
            case EQ:
                return equality("eq", l, r);
            case NE:
                return equality("ne", l, r);
            case AND:
                return booleans("and", l, r);
            case OR:
                return booleans("or", l, r);
            default:
                throw new UnsupportedOperationException(operator.toString());
        }
    }

    private static MethodCallExpr arithmetic(String op, Expression left, Expression right) {
        return new MethodCallExpr(null, op, new NodeList<>(left, right));
    }

    private static MethodCallExpr equality(String op, Expression left, Expression right) {
        return new MethodCallExpr(null, op, new NodeList<>(left, right));
    }

    private static MethodCallExpr comparison(String op, Expression left, Expression right) {
        return new MethodCallExpr(null, op, new NodeList<>(left, right));
    }

    private static MethodCallExpr booleans(String op, Expression left, Expression right) {
        Expression l = coerceToBoolean(left);
        Expression r = coerceToBoolean(right);

        return new MethodCallExpr(null, op, new NodeList<>(l, r));
    }

    public static MethodCallExpr unary(
            UnaryTestNode.UnaryOperator operator,
            Expression right) {
        EnclosedExpr l = castTo(BigDecimalT, LEFT_EXPR);
        EnclosedExpr r = castTo(BigDecimalT, right);
        return new MethodCallExpr(l, toFunctionName(operator), new NodeList<>(r, DECIMAL_128));
    }

    private static String toFunctionName(UnaryTestNode.UnaryOperator operator) {
        switch (operator) {
            case LTE:
                return "lte";
            case LT:
                return "lt";
            case GT:
                return "gt";
            case GTE:
                return "gte";
            case EQ:
                return "eq";
            case NE:
                return "ne";
            case IN:
                throw new UnsupportedOperationException(operator.toString());
            case NOT:
                throw new UnsupportedOperationException(operator.toString());
            case TEST:
                throw new UnsupportedOperationException(operator.toString());
            default:
                throw new UnsupportedOperationException(operator.toString());
        }
    }

    public static MethodCallExpr lt(Expression left, Expression right) {
        return new MethodCallExpr(null, "lt")
                .addArgument(left)
                .addArgument(right);
    }

    public static MethodCallExpr gt(Expression left, Expression right) {
        return new MethodCallExpr(null, "gt")
                .addArgument(left)
                .addArgument(right);
    }

    public static MethodCallExpr between(Expression value, Expression start, Expression end) {
        return new MethodCallExpr(null, "between")
                .addArgument(FeelCtx.FEELCTX)
                .addArgument(value)
                .addArgument(start)
                .addArgument(end);
    }

    public static EnclosedExpr castTo(Type type, Expression expr) {
        return new EnclosedExpr(new CastExpr(type, new EnclosedExpr(expr)));
    }

    public static MethodCallExpr reflectiveCastTo(Type type, Expression expr) {
        return new MethodCallExpr(new ClassExpr(type), "cast")
                .addArgument(new EnclosedExpr(expr));
    }

    public static Expression quantifier(
            QuantifiedExpressionNode.Quantifier quantifier,
            Expression condition,
            List<Expression> iterationContexts) {

        // quant({SOME,EVERY}, FEELCTX)
        MethodCallExpr quant =
                new MethodCallExpr(Expressions.STDLIB, "quant")
                        .addArgument(quantifier == QuantifiedExpressionNode.Quantifier.SOME ?
                                             QUANTIFIER_SOME :
                                             QUANTIFIER_EVERY)
                        .addArgument(FeelCtx.FEELCTX);

        // .with(expr)
        // .with(expr)
        Expression chainedCalls = iterationContexts.stream()
                .reduce(quant, (l, r) -> r.asMethodCallExpr().setScope(l));

        return new MethodCallExpr(chainedCalls, "satisfies")
                .addArgument(condition);
    }

    public static MethodCallExpr ffor(
            List<Expression> iterationContexts,
            Expression returnExpr) {
        MethodCallExpr ffor =
                new MethodCallExpr(Expressions.STDLIB, "ffor")
                        .addArgument(FeelCtx.FEELCTX);

        // .with(expr)
        // .with(expr)
        Expression chainedCalls = iterationContexts.stream()
                .reduce(ffor, (l, r) -> r.asMethodCallExpr().setScope(l));

        return new MethodCallExpr(chainedCalls, "rreturn")
                .addArgument(returnExpr);
    }

    public static MethodCallExpr range(RangeNode.IntervalBoundary lowBoundary,
                                       Expression lowEndPoint,
                                       Expression highEndPoint,
                                       RangeNode.IntervalBoundary highBoundary) {

        return new MethodCallExpr(null, "range")
                .addArgument(FeelCtx.FEELCTX)
                .addArgument(Constants.rangeBoundary(lowBoundary))
                .addArgument(lowEndPoint)
                .addArgument(highEndPoint)
                .addArgument(Constants.rangeBoundary(highBoundary));
    }

    public static MethodCallExpr includes(Expression range, Expression target) {
        return new MethodCallExpr(null, "includes")
                .addArgument(FeelCtx.FEELCTX)
                .addArgument(range)
                .addArgument(target);
    }

    public static MethodCallExpr exists(Expression tests, Expression target) {
        return new MethodCallExpr(null, "exists")
                .addArgument(FeelCtx.FEELCTX)
                .addArgument(tests)
                .addArgument(target);
    }

    public static MethodCallExpr notExists(Expression expr) {
        return new MethodCallExpr(null, "notExists")
                .addArgument(FeelCtx.FEELCTX)
                .addArgument(expr)
                .addArgument(LEFT_EXPR);
    }

    public static NamedLambda namedLambda(Expression expr, String text) {
        LambdaExpr lambda = Expressions.lambda(expr);
        String name = Constants.functionName(text);
        FieldDeclaration field = Constants.function(name, lambda);
        return new NamedLambda(new NameExpr(name), lambda, field);
    }

    public static LambdaExpr lambda(Expression expr) {
        return new LambdaExpr(
                new NodeList<>(
                        new Parameter(UNKNOWN_TYPE, FeelCtx.FEELCTX_N)),
                new ExpressionStmt(expr),
                true);
    }

    public static LambdaExpr unaryLambda(Expression expr) {
        return new LambdaExpr(
                new NodeList<>(
                        new Parameter(UNKNOWN_TYPE, FeelCtx.FEELCTX_N),
                        new Parameter(UNKNOWN_TYPE, "left")),
                new ExpressionStmt(expr),
                true);
    }

    public static ObjectCreationExpr namedParameter(Expression name, Expression value) {
        return new ObjectCreationExpr(null, NamedParamterT, new NodeList<>(name, value));
    }

    public static MethodCallExpr invoke(Expression functionName, Expression params) {
        return new MethodCallExpr(STDLIB, "invoke")
                .addArgument(FeelCtx.FEELCTX)
                .addArgument(functionName)
                .addArgument(params);
    }

    public static MethodCallExpr filter(Expression expr, Expression filter) {
        return new MethodCallExpr(new MethodCallExpr(STDLIB, "filter")
                                          .addArgument(FeelCtx.FEELCTX)
                                          .addArgument(expr),
                                  "with")
                .addArgument(filter);
    }

    public static MethodCallExpr path(Expression expr, Expression filter) {
        return new MethodCallExpr(new MethodCallExpr(STDLIB, "path")
                                          .addArgument(FeelCtx.FEELCTX)
                                          .addArgument(expr),
                                  "with")
                .addArgument(filter);
    }

    public static MethodCallExpr path(Expression expr, List<Expression> filters) {
        MethodCallExpr methodCallExpr = new MethodCallExpr(new MethodCallExpr(STDLIB, "path")
                                                                   .addArgument(FeelCtx.FEELCTX)
                                                                   .addArgument(expr),
                                                           "with");
        filters.forEach(methodCallExpr::addArgument);
        return methodCallExpr;
    }

    public static MethodCallExpr isInstanceOf(Expression expr, Expression type) {
        return new MethodCallExpr(type, "isInstanceOf")
                .addArgument(expr);
    }

    public static MethodCallExpr nativeInstanceOf(Type type, Expression condition) {
        return new MethodCallExpr(
                new ClassExpr(type),
                "isInstance")
                .addArgument(new EnclosedExpr(condition));
    }

    public static MethodCallExpr determineTypeFromName(String typeAsText) {
        return new MethodCallExpr(BuiltInTypeT, "determineTypeFromName")
                .addArgument(new StringLiteralExpr(typeAsText));
    }

    public static Expression contains(Expression expr, Expression value) {
        return new MethodCallExpr(expr, "contains")
                .addArgument(value);
    }

    public static Expression coerceToString(Expression expression) {
        return new MethodCallExpr(new NameExpr("String"), "valueOf").addArgument(expression);
    }

    public static Expression coerceToBoolean(Expression expression) {
        return new MethodCallExpr(null, "coerceToBoolean")
                .addArgument(FeelCtx.FEELCTX)
                .addArgument(expression);
    }

    public static MethodCallExpr coerceNumber(Expression exprCursor) {
        MethodCallExpr coerceNumberMethodCallExpr = new MethodCallExpr(new NameExpr(CompiledFEELSupport.class.getSimpleName()), "coerceNumber");
        coerceNumberMethodCallExpr.addArgument(exprCursor);
        return coerceNumberMethodCallExpr;
    }
}


