package org.kie.dmn.feel.lang.impl;

import java.util.Collections;
import java.util.List;

import org.kie.dmn.feel.lang.EvaluationContext;
import org.kie.dmn.feel.runtime.UnaryTest;

public class UnaryTestInterpretedExecutableExpression {

    public static final UnaryTestInterpretedExecutableExpression EMPTY = new UnaryTestInterpretedExecutableExpression(null) {
        @Override
        public List<UnaryTest> evaluate() {
            return Collections.emptyList();
        }
    };
    private final CompiledExpressionImpl expr;

    public UnaryTestInterpretedExecutableExpression(CompiledExpressionImpl expr) {
        this.expr = expr;
    }

    public List<UnaryTest> evaluate() {
        return (List<UnaryTest>) expr.evaluate(null);
    }
}
