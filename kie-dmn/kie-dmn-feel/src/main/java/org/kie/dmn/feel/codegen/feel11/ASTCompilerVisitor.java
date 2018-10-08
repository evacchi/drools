package org.kie.dmn.feel.codegen.feel11;

import java.util.ArrayList;
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

import static org.kie.dmn.feel.codegen.feel11.DirectCompilerResult.mergeFDs;

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
        MethodCallExpr expr = Expressions.binary(
                n.getOperator(),
                left.getExpression(),
                right.getExpression());
        return DirectCompilerResult.of(expr, BuiltInType.UNKNOWN).withFD(left).withFD(right);
    }

    @Override
    public DirectCompilerResult visit(InstanceOfNode n) {
        DirectCompilerResult expr = n.getExpression().accept(this);
        DirectCompilerResult type = n.getType().accept(this);
        return DirectCompilerResult.of(
                Expressions.isInstanceOf(expr.getExpression(), type.getExpression()),
                BuiltInType.BOOLEAN,
                mergeFDs(expr, type));
    }

    @Override
    public DirectCompilerResult visit(TypeNode n) {
        return DirectCompilerResult.of(
                Expressions.determineTypeFromName(n.getText()),
                BuiltInType.UNKNOWN);
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
        DirectCompilerResult expr = n.getExpression().accept(this);
        // DirectCompilerResult name = n.getName().accept(this);
        DirectCompilerResult filter = n.getFilter().accept(this);

        Expression lambda = Expressions.lambda(filter.getExpression());
        return DirectCompilerResult.of(
                Expressions.filter(expr.getExpression(), lambda),
                // here we could still try to infer the result type, but presently use ANY
                BuiltInType.UNKNOWN).withFD(expr).withFD(filter);
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

        if (exprs.resultType == BuiltInType.LIST) {
            return DirectCompilerResult.of(
                    Expressions.exists(exprs.getExpression(), value.getExpression()),
                    BuiltInType.BOOLEAN).withFD(value).withFD(exprs);
        } else if (exprs.resultType == BuiltInType.RANGE) {
            return DirectCompilerResult.of(
                    Expressions.includes(exprs.getExpression(), value.getExpression()),
                    BuiltInType.BOOLEAN).withFD(value).withFD(exprs);
        } else {
            // this should be turned into a tree rewrite
            return DirectCompilerResult.of(
                    Expressions.exists(exprs.getExpression(), value.getExpression()),
                    BuiltInType.BOOLEAN).withFD(value).withFD(exprs);
        }
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
                Expressions.path(expr.getExpression(), nameRef),
                // here we could still try to infer the result type, but presently use ANY
                BuiltInType.UNKNOWN).withFD(expr);
    }

    @Override
    public DirectCompilerResult visit(QuantifiedExpressionNode n) {
        DirectCompilerResult expr = n.getExpression().accept(this);
        LambdaExpr eL = Expressions.lambda(expr.getExpression());
        String eN = Constants.functionName(n.getExpression().getText());
        FieldDeclaration field = Constants.function(eN, eL);


        ArrayList<Expression> names = new ArrayList<>();
        ArrayList<Expression> exprs = new ArrayList<>();
        HashSet<FieldDeclaration> fds = new HashSet<>();

        fds.add(field);
        fds.addAll(expr.getFieldDeclarations());
        for (IterationContextNode iterCtx : n.getIterationContexts()) {
            DirectCompilerResult iterName = iterCtx.getName().accept(this);
            DirectCompilerResult iterExpr = iterCtx.getExpression().accept(this);
            LambdaExpr nameL = Expressions.lambda(iterName.getExpression());
            LambdaExpr exprL = Expressions.lambda(iterExpr.getExpression());
            String nameFieldName = Constants.functionName(iterCtx.getName().getText());
            String exprFieldName = Constants.functionName(iterCtx.getExpression().getText());
            FieldDeclaration fnName = Constants.function(nameFieldName, nameL);
            FieldDeclaration fnExpr = Constants.function(exprFieldName, exprL);
            names.add(new NameExpr(nameFieldName));
            exprs.add(new NameExpr(exprFieldName));
            fds.add(fnName);
            fds.add(fnExpr);
            fds.addAll(iterName.getFieldDeclarations());
            fds.addAll(iterExpr.getFieldDeclarations());
        }

        return DirectCompilerResult.of(
                Expressions.quantifier(
                        n.getQuantifier(), new NameExpr(eN), names, exprs),
                expr.resultType,
                fds);

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
        MethodCallExpr expr = Expressions.unary(n.getOperator(), value.getExpression());
        LambdaExpr lambda = Expressions.unaryLambda(expr);
        String utName = Constants.unaryTestName(n.getText());
        FieldDeclaration ut = Constants.unaryTest(utName, lambda);
        HashSet<FieldDeclaration> fds = new HashSet<>(value.getFieldDeclarations());
        fds.add(ut);
        return DirectCompilerResult.of(new NameExpr(utName), BuiltInType.UNARY_TEST, fds);
    }
}
