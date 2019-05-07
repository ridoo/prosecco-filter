package org.n52.prosecco.filter;

import java.util.Set;

import org.n52.prosecco.web.request.FilterContext;

interface FilterContextFilter<T> {

    Set<T> evaluate(Set<T> values, FilterContext context);
}
