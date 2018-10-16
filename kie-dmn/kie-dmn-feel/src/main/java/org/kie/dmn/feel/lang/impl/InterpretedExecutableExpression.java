package org.kie.dmn.feel.lang.impl;

import org.kie.dmn.feel.lang.CompiledExpression;
import org.kie.dmn.feel.lang.EvaluationContext;
import org.kie.dmn.feel.lang.ast.FunctionDefNode;

public class InterpretedExecutableExpression implements ExecutableExpression,
                                                        CompiledExpression {

    private final CompiledExpressionImpl expr;

    public InterpretedExecutableExpression(CompiledExpressionImpl expr) {
        this.expr = expr;
    }

    public boolean isFunctionOhNoWhyAreYouDoingThis() {
        return expr.getExpression() instanceof FunctionDefNode;
    }

    @Override
    public Object evaluate(EvaluationContext evaluationContext) {
        return expr.evaluate(evaluationContext);
    }
}
