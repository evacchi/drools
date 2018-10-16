package org.kie.dmn.feel.lang.impl;

import org.kie.dmn.feel.lang.EvaluationContext;

public interface ExecutableExpression {

    Object evaluate(EvaluationContext evaluationContext);
}
