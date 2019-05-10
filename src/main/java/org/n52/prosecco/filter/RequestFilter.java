package org.n52.prosecco.filter;

import java.util.Set;

interface RequestFilter<T> {

    Set<T> evaluate(String parameter, Set<T> values);
}
