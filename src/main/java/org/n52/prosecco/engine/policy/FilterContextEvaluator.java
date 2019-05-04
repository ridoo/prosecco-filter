package org.n52.prosecco.engine.policy;

import java.util.Set;

import org.n52.prosecco.engine.filter.FilterContext;

public interface FilterContextEvaluator<T> {

    Set<T> evaluate(Set<T> values, FilterContext context);
}
