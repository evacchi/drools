package org.kie.dmn.feel.codegen.feel11;

import org.drools.javaparser.ast.NodeList;
import org.drools.javaparser.ast.body.Parameter;
import org.drools.javaparser.ast.expr.CastExpr;
import org.drools.javaparser.ast.expr.EnclosedExpr;
import org.drools.javaparser.ast.expr.Expression;
import org.drools.javaparser.ast.expr.LambdaExpr;
import org.drools.javaparser.ast.expr.MethodCallExpr;
import org.drools.javaparser.ast.expr.NameExpr;
import org.drools.javaparser.ast.stmt.ExpressionStmt;
import org.drools.javaparser.ast.type.Type;
import org.drools.javaparser.ast.type.UnknownType;
import org.kie.dmn.feel.lang.ast.RangeNode;

import static org.kie.dmn.feel.codegen.feel11.Constants.BigDecimalT;
import static org.kie.dmn.feel.codegen.feel11.Constants.DECIMAL_128;

public class Expressions {

    public static final String LEFT = "left";
    public static final NameExpr LEFT_EXPR = new NameExpr(LEFT);
    public static final UnknownType UNKNOWN_TYPE = new UnknownType();
    public static final NameExpr STDLIB = new NameExpr(CompiledFEELSupport.class.getSimpleName());

    public static Expression negate(Expression expression) {
        EnclosedExpr e = castTo(BigDecimalT, expression);
        return new MethodCallExpr(e, "negate");
    }

    public static MethodCallExpr add(Expression left, Expression right) {
        EnclosedExpr l = castTo(BigDecimalT, left);
        EnclosedExpr r = castTo(BigDecimalT, right);
        return new MethodCallExpr(l, "add", new NodeList<>(r, DECIMAL_128));
    }

    public static MethodCallExpr mult(Expression left, Expression right) {
        EnclosedExpr l = castTo(BigDecimalT, left);
        EnclosedExpr r = castTo(BigDecimalT, right);
        return new MethodCallExpr(l, "mult", new NodeList<>(r, DECIMAL_128));
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

    private static EnclosedExpr castTo(Type type, Expression expr) {
        return new EnclosedExpr(new CastExpr(type, new EnclosedExpr(expr)));
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
                        new Parameter(UNKNOWN_TYPE, "feelExprCtx"),
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
        return new MethodCallExpr(new MethodCallExpr(STDLIB, "path")
                                          .addArgument(FeelCtx.FEELCTX)
                                          .addArgument(expr),
                                  "with")
                .addArgument(filter);
    }

    public static MethodCallExpr unaryLt(Expression expression) {
        return null;
    }
}


