package org.kie.dmn.feel.codegen.feel11;

import java.util.HashSet;

import org.drools.javaparser.ast.body.FieldDeclaration;
import org.drools.javaparser.ast.expr.BooleanLiteralExpr;
import org.drools.javaparser.ast.expr.Expression;
import org.drools.javaparser.ast.expr.LambdaExpr;
import org.drools.javaparser.ast.expr.MethodCallExpr;
import org.drools.javaparser.ast.expr.NameExpr;
import org.drools.javaparser.ast.expr.NullLiteralExpr;
import org.drools.javaparser.ast.expr.StringLiteralExpr;
import org.kie.dmn.feel.lang.ast.ASTNode;
import org.kie.dmn.feel.lang.ast.BaseNode;
import org.kie.dmn.feel.lang.ast.BetweenNode;
import org.kie.dmn.feel.lang.ast.BooleanNode;
import org.kie.dmn.feel.lang.ast.ContextEntryNode;
import org.kie.dmn.feel.lang.ast.ContextNode;
import org.kie.dmn.feel.lang.ast.DashNode;
import org.kie.dmn.feel.lang.ast.FilterExpressionNode;
import org.kie.dmn.feel.lang.ast.ForExpressionNode;
import org.kie.dmn.feel.lang.ast.FunctionDefNode;
import org.kie.dmn.feel.lang.ast.FunctionInvocationNode;
import org.kie.dmn.feel.lang.ast.IfExpressionNode;
import org.kie.dmn.feel.lang.ast.InNode;
import org.kie.dmn.feel.lang.ast.InfixOpNode;
import org.kie.dmn.feel.lang.ast.InstanceOfNode;
import org.kie.dmn.feel.lang.ast.IterationContextNode;
import org.kie.dmn.feel.lang.ast.ListNode;
import org.kie.dmn.feel.lang.ast.NameDefNode;
import org.kie.dmn.feel.lang.ast.NameRefNode;
import org.kie.dmn.feel.lang.ast.NamedParameterNode;
import org.kie.dmn.feel.lang.ast.NullNode;
import org.kie.dmn.feel.lang.ast.NumberNode;
import org.kie.dmn.feel.lang.ast.PathExpressionNode;
import org.kie.dmn.feel.lang.ast.QualifiedNameNode;
import org.kie.dmn.feel.lang.ast.QuantifiedExpressionNode;
import org.kie.dmn.feel.lang.ast.RangeNode;
import org.kie.dmn.feel.lang.ast.SignedUnaryNode;
import org.kie.dmn.feel.lang.ast.StringNode;
import org.kie.dmn.feel.lang.ast.TypeNode;
import org.kie.dmn.feel.lang.ast.UnaryTestNode;
import org.kie.dmn.feel.lang.ast.Visitor;
import org.kie.dmn.feel.lang.impl.MapBackedType;
import org.kie.dmn.feel.lang.types.BuiltInType;
import org.kie.dmn.feel.util.EvalHelper;

public class ASTCompilerVisitor implements Visitor<DirectCompilerResult> {

    @Override
    public DirectCompilerResult visit(ASTNode n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DirectCompilerResult visit(DashNode n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DirectCompilerResult visit(BooleanNode n) {
        return DirectCompilerResult.of(new BooleanLiteralExpr(n.getValue()), BuiltInType.BOOLEAN);
    }

    @Override
    public DirectCompilerResult visit(NumberNode n) {
        String originalText = n.getText();
        String constantName = Constants.numericName(originalText);
        FieldDeclaration constant = Constants.numeric(constantName, originalText);
        return DirectCompilerResult.of(
                new NameExpr(constantName),
                BuiltInType.NUMBER,
                constant);
    }

    @Override
    public DirectCompilerResult visit(StringNode n) {
        StringLiteralExpr expr = new StringLiteralExpr();
        String actualStringContent = n.getText();
        actualStringContent = actualStringContent.substring(1, actualStringContent.length() - 1); // remove start/end " from the FEEL text expression.
        String unescaped = EvalHelper.unescapeString(actualStringContent); // unescapes String, FEEL-style
        expr.setString(unescaped); // setString escapes the contents Java-style
        return DirectCompilerResult.of(expr, BuiltInType.STRING);
    }

    @Override
    public DirectCompilerResult visit(NullNode n) {
        return DirectCompilerResult.of(new NullLiteralExpr(), BuiltInType.UNKNOWN);
    }

    @Override
    public DirectCompilerResult visit(TypeNode n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DirectCompilerResult visit(NameDefNode n) {
        StringLiteralExpr expr = new StringLiteralExpr(EvalHelper.normalizeVariableName(n.getText()));
        return DirectCompilerResult.of(expr, BuiltInType.STRING);
    }

    @Override
    public DirectCompilerResult visit(NameRefNode n) {
        String nameRef = EvalHelper.normalizeVariableName(n.getText());
        return DirectCompilerResult.of(FeelCtx.getValue(nameRef), BuiltInType.UNKNOWN);
    }

    @Override
    public DirectCompilerResult visit(QualifiedNameNode n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DirectCompilerResult visit(InfixOpNode n) {
        DirectCompilerResult left = n.getLeft().accept(this);
        DirectCompilerResult right = n.getRight().accept(this);
        MethodCallExpr expr = null;
        switch (n.getOperator()) {
            case ADD:
                expr = Expressions.add(left.getExpression(), right.getExpression());
                break;
            case SUB:
                break;
            case MULT:
                expr = Expressions.mult(left.getExpression(), right.getExpression());
                break;
            case DIV:
                break;
            case POW:
                break;
            case LTE:
                break;
            case LT:
                break;
            case GT:
                break;
            case GTE:
                break;
            case EQ:
                expr = Expressions.eq(left.getExpression(), right.getExpression());
                break;
            case NE:
                break;
            case AND:
                break;
            case OR:
                break;
            default:
        }
        if (expr == null) //fixme temp
            throw new UnsupportedOperationException("Not yet implemented: " + n.getOperator());

        return DirectCompilerResult.of(expr, BuiltInType.UNKNOWN).withFD(left).withFD(right);
    }

    @Override
    public DirectCompilerResult visit(InstanceOfNode n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DirectCompilerResult visit(IfExpressionNode n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DirectCompilerResult visit(ForExpressionNode n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DirectCompilerResult visit(BetweenNode n) {
        DirectCompilerResult value = n.getValue().accept(this);
        DirectCompilerResult start = n.getStart().accept(this);
        DirectCompilerResult end = n.getEnd().accept(this);

        return DirectCompilerResult.of(
                Expressions.between(
                        value.getExpression(),
                        start.getExpression(),
                        end.getExpression()),
                BuiltInType.BOOLEAN)
                .withFD(value)
                .withFD(start)
                .withFD(end);
    }

    @Override
    public DirectCompilerResult visit(ContextNode n) {
        if (n.getEntries().isEmpty()) {
            return DirectCompilerResult.of(
                    FeelCtx.emptyContext(), BuiltInType.CONTEXT);
        }

        // openContext(feelCtx)
        MapBackedType resultType = new MapBackedType();
        DirectCompilerResult openContext =
                DirectCompilerResult.of(FeelCtx.openContext(), resultType);

        //   .setEntry( k,v )
        //   .setEntry( k,v )
        //   ...
        DirectCompilerResult entries = n.getEntries()
                .stream()
                .map(e -> e.accept(this))
                .reduce(openContext,
                        (l, r) -> DirectCompilerResult.of(
                                r.getExpression().asMethodCallExpr().setScope(l.getExpression()),
                                r.resultType,
                                DirectCompilerResult.mergeFDs(l, r)));

        // .closeContext()
        return DirectCompilerResult.of(
                FeelCtx.closeContext(entries),
                resultType,
                entries.getFieldDeclarations());
    }

    @Override
    public DirectCompilerResult visit(ContextEntryNode n) {
        DirectCompilerResult key = n.getName().accept(this);
        DirectCompilerResult value = n.getValue().accept(this);
        if (key.resultType != BuiltInType.STRING) {
            throw new IllegalArgumentException(
                    "a Context Entry Key must be a valid FEEL String type");
        }
        String keyText = key.getExpression().asStringLiteralExpr().getValue();

        // .setEntry(key, value)
        MethodCallExpr setEntryContextCall =
                FeelCtx.setEntry(keyText, value.getExpression());

        return DirectCompilerResult.of(
                setEntryContextCall,
                value.resultType,
                value.getFieldDeclarations());
    }

    @Override
    public DirectCompilerResult visit(FilterExpressionNode n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DirectCompilerResult visit(FunctionDefNode n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DirectCompilerResult visit(FunctionInvocationNode n) {
        DirectCompilerResult functionName = n.getName().accept(this);
        DirectCompilerResult params = n.getParams().accept(this);
        return DirectCompilerResult.of(
                Expressions.invoke(functionName.getExpression(), params.getExpression()),
                functionName.resultType)
                .withFD(functionName)
                .withFD(params);
    }

    @Override
    public DirectCompilerResult visit(NamedParameterNode n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DirectCompilerResult visit(InNode n) {
        DirectCompilerResult value = n.getValue().accept(this);
        DirectCompilerResult exprs = n.getExprs().accept(this);

        return DirectCompilerResult.of(
                Expressions.exists(exprs.getExpression(), value.getExpression()),
                BuiltInType.BOOLEAN).withFD(value).withFD(exprs);
    }

    @Override
    public DirectCompilerResult visit(IterationContextNode n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DirectCompilerResult visit(ListNode n) {
        MethodCallExpr list = new MethodCallExpr(null, "list");
        DirectCompilerResult result = DirectCompilerResult.of(list, BuiltInType.LIST);

        for (BaseNode e : n.getElements()) {
            DirectCompilerResult r = e.accept(this);
            result.withFD(r.getFieldDeclarations());
            list.addArgument(r.getExpression());
        }

        return result;
    }

    @Override
    public DirectCompilerResult visit(PathExpressionNode n) {
        DirectCompilerResult expr = n.getExpression().accept(this);
        // DirectCompilerResult name = n.getName().accept(this);
        Expression nameRef = new StringLiteralExpr(n.getName().getText());

        return DirectCompilerResult.of(
                Expressions.filter(expr.getExpression(), nameRef),
                // here we could still try to infer the result type, but presently use ANY
                BuiltInType.UNKNOWN).withFD(expr);
    }

    @Override
    public DirectCompilerResult visit(QuantifiedExpressionNode n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public DirectCompilerResult visit(RangeNode n) {
        DirectCompilerResult start = n.getStart().accept(this);
        DirectCompilerResult end = n.getEnd().accept(this);
        return DirectCompilerResult.of(
                Expressions.range(
                        n.getLowerBound(),
                        start.getExpression(),
                        end.getExpression(),
                        n.getUpperBound()),
                BuiltInType.RANGE,
                DirectCompilerResult.mergeFDs(start, end));

    }

    @Override
    public DirectCompilerResult visit(SignedUnaryNode n) {
        DirectCompilerResult result = n.getExpression().accept(this);
        if (n.getSign() == SignedUnaryNode.Sign.NEGATIVE) {
            return DirectCompilerResult.of(
            Expressions.negate(result.getExpression()),
                    result.resultType,
                    result.getFieldDeclarations());
        } else {
            return result;
        }
    }

    @Override
    public DirectCompilerResult visit(UnaryTestNode n) {
        DirectCompilerResult value = n.getValue().accept(this);
        MethodCallExpr expr = null;
        switch (n.getOperator()) {
            case NOT:
                expr = Expressions.notExists(value.getExpression());
            case LTE:
                break;
            case LT:
                expr = Expressions.lt(Expressions.LEFT_EXPR,  value.getExpression());
            case GT:
                expr = Expressions.gt(Expressions.LEFT_EXPR,  value.getExpression());
            case GTE:
                break;
            case NE:
                break;
            case EQ:
                break;
            case IN:
                break;
            case TEST:
                break;
            default:
                break;
        }
        if (expr == null) throw new UnsupportedOperationException(n.getOperator().toString());
        LambdaExpr lambda = Expressions.lambda(expr);
        String utName = Constants.unaryTestName(n.getText());
        FieldDeclaration ut = Constants.unaryTest(utName, lambda);
        HashSet<FieldDeclaration> fds = new HashSet<>(value.getFieldDeclarations());
        fds.add(ut);
        return DirectCompilerResult.of(new NameExpr(utName), BuiltInType.UNARY_TEST, fds);

    }
}
