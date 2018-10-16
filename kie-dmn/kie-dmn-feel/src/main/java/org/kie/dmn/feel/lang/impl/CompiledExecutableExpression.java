package org.kie.dmn.feel.lang.impl;

import org.kie.dmn.feel.codegen.feel11.CompiledFEELExpression;
import org.kie.dmn.feel.lang.CompiledExpression;
import org.kie.dmn.feel.lang.EvaluationContext;

public class CompiledExecutableExpression implements ExecutableExpression,
                                                     CompiledFEELExpression {

    private final CompiledFEELExpression expr;

    public CompiledExecutableExpression(CompiledFEELExpression expr) {
        this.expr = expr;
    }

    @Override
    public Object evaluate(EvaluationContext evaluationContext) {
        return expr.apply(evaluationContext);
    }

    @Override
    public Object apply(EvaluationContext evaluationContext) {
        return evaluate(evaluationContext);
    }
}
