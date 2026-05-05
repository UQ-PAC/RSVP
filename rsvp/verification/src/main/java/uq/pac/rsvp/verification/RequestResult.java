package uq.pac.rsvp.verification;

import uq.pac.rsvp.policy.ast.policy.Policy;

import java.util.HashSet;
import java.util.Set;

public class RequestResult {
    public boolean permitted;
    public Set<Policy> policies = new HashSet<>();
}
