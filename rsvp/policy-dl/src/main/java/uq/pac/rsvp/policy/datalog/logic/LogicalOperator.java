/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.datalog.logic;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uq.pac.rsvp.Assertion.require;

public abstract class LogicalOperator extends Formula {
    private final List<Formula> formulae;

    public LogicalOperator(Formula ...formulae) {
        require(formulae.length >= 2);
        this.formulae = Arrays.stream(formulae).toList();
    }

    abstract String getStringOperator();

    protected String stringify() {
        return "(" + formulae.stream()
                .map(Formula::toString)
                .collect(Collectors.joining(" " + getStringOperator() + " ")) + ")";
    }

    public Formula get(int index) {
        return formulae.get(index);
    }

    public int arity() {
        return formulae.size();
    }

    public List<Formula> getFormulae() {
        return formulae;
    }

    public Stream<Formula> formulae() {
        return formulae.stream();
    }
}
