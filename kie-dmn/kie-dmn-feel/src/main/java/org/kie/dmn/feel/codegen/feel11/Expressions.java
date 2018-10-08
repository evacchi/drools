package org.kie.dmn.feel.codegen.feel11;

import java.util.List;
import java.util.Map;

import org.drools.javaparser.JavaParser;
import org.drools.javaparser.ast.NodeList;
import org.drools.javaparser.ast.body.Parameter;
import org.drools.javaparser.ast.expr.CastExpr;
import org.drools.javaparser.ast.expr.EnclosedExpr;
import org.drools.javaparser.ast.expr.Expression;
import org.drools.javaparser.ast.expr.LambdaExpr;
import org.drools.javaparser.ast.expr.MethodCallExpr;
import org.drools.javaparser.ast.expr.NameExpr;
import org.drools.javaparser.ast.expr.StringLiteralExpr;
import org.drools.javaparser.ast.stmt.ExpressionStmt;
import org.drools.javaparser.ast.type.Type;
import org.drools.javaparser.ast.type.UnknownType;
import org.kie.dmn.feel.lang.ast.InfixOpNode;
import org.kie.dmn.feel.lang.ast.QuantifiedExpressionNode;
import org.kie.dmn.feel.lang.ast.RangeNode;
import org.kie.dmn.feel.lang.ast.UnaryTestNode;

import static org.kie.dmn.feel.codegen.feel11.Constants.BigDecimalT;
import static org.kie.dmn.feel.codegen.feel11.Constants.BuiltInTypeT;
import static org.kie.dmn.feel.codegen.feel11.Constants.DECIMAL_128;

public class Expressions {

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
            Expression left,
            Expression right) {
        EnclosedExpr l = castTo(BigDecimalT, left);
        EnclosedExpr r = castTo(BigDecimalT, right);
        switch (operator) {
            case ADD:
                return arithmetic("add", l, r);
            case SUB:
                return arithmetic("subtract", l, r);
            case MULT:
                return arithmetic("multiply", l, r);
            case DIV:
                return arithmetic("divide", l, r);
            case POW:
                return arithmetic("pow", l,
                                  new MethodCallExpr(r, "intValue"));

            case LTE:
                return comparison("lte", l, r);
            case LT:
                return comparison("lt", l, r);
            case GT:
                return comparison("gt", l, r);
            case GTE:
                return comparison("gte", l, r);
            case EQ:
                return comparison("eq", l, r);
            case NE:
                return comparison("ne", l, r);
            case AND:
                return comparison("and", l, r);
            case OR:
                return comparison("or", l, r);
            default:
                throw new UnsupportedOperationException(operator.toString());
        }
    }

    private static MethodCallExpr comparison(String op, Expression l, Expression r) {
        return new MethodCallExpr(null, op, new NodeList<>(l, r));
    }

    private static MethodCallExpr arithmetic(String op, Expression l, Expression r) {
        return new MethodCallExpr(l, op, new NodeList<>(r, DECIMAL_128));
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

    public static MethodCallExpr eq(Expression left, Expression right) {
        return new MethodCallExpr(null, "eq")
                .addArgument(left)
                .addArgument(right);
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

    public static Expression quantifier(
            QuantifiedExpressionNode.Quantifier quantifier,
            Expression quant,
            List<Expression> names,
            List<Expression> exprs) {
        MethodCallExpr forCall = new MethodCallExpr(STDLIB, "quant");
        forCall.addArgument(quantifier == QuantifiedExpressionNode.Quantifier.SOME ? QUANTIFIER_SOME : QUANTIFIER_EVERY);
        forCall.addArgument(FeelCtx.FEELCTX);
        Expression curForCallTail = forCall;
        for (int i = 0; i < exprs.size(); i++) {
            Expression name = names.get(i);
            Expression expr = exprs.get(i);
            curForCallTail = new MethodCallExpr(curForCallTail, "with")
                    .addArgument((name))
                    .addArgument((expr));
        }
        MethodCallExpr returnCall = new MethodCallExpr(curForCallTail, "satisfies");
        Expression returnParam = (quant);
        returnCall.addArgument(returnParam);
        return returnCall;
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

    public static MethodCallExpr coerceNumber(Expression exprCursor) {
        MethodCallExpr coerceNumberMethodCallExpr = new MethodCallExpr(new NameExpr(CompiledFEELSupport.class.getSimpleName()), "coerceNumber");
        coerceNumberMethodCallExpr.addArgument(exprCursor);
        return coerceNumberMethodCallExpr;
    }
}


