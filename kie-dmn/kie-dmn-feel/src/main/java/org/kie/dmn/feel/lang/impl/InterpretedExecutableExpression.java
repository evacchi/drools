package org.kie.dmn.feel.lang.impl;

import org.kie.dmn.feel.lang.CompiledExpression;
import org.kie.dmn.feel.lang.EvaluationContext;

public class InterpretedExecutableExpression implements ExecutableExpression,
                                                        CompiledExpression {

    private final CompiledExpressionImpl expr;

    public InterpretedExecutableExpression(CompiledExpressionImpl expr) {
        this.expr = expr;
    }

    @Override
    public Object evaluate(EvaluationContext evaluationContext) {
        return expr.evaluate(evaluationContext);
    }
}
