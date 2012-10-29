package org.vanda.studio.model.types;

import java.util.HashMap;
import java.util.Map;

import org.vanda.studio.util.TokenSource.Token;

public final class Equation {

	public final Type lhs;
	public final Type rhs;

	public Equation(Type lhs, Type rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Equation) {
			Equation o = (Equation) other;
			return (lhs.equals(o.lhs) && rhs.equals(o.rhs));
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return lhs.hashCode() + rhs.hashCode();
	}

	public boolean canDecompose() {
		return (lhs instanceof CompositeType && rhs instanceof CompositeType);
	}

	public boolean canEliminate() {
		return (lhs instanceof TypeVariable && lhs.equals(rhs));
	}

	public boolean canFlip() {
		return (lhs instanceof CompositeType && rhs instanceof TypeVariable);
	}

	public boolean canSubstitute() {
		return (lhs instanceof TypeVariable);
	}

	public void decompose(Map<Equation, Token> t, Token addr) {
		CompositeType l = (CompositeType) lhs;
		CompositeType r = (CompositeType) rhs;
		for (int i = 0; i < l.children.size(); i++) {
			t.put(new Equation(l.children.get(i), r.children.get(i)), addr);
		}
	}

	public boolean failsDecomposeCheck() {
		return (!((CompositeType) lhs).constructor
				.equals(((CompositeType) rhs).constructor))
				|| ((CompositeType) lhs).children.size() != ((CompositeType) rhs).children
						.size();
	}

	public boolean failsOccursCheck() {
		if (lhs instanceof TypeVariable)
			return rhs.contains(((TypeVariable) lhs).variable);
		else
			return false;
	}

	public void flip(Map<Equation, Token> t, Token addr) {
		t.put(new Equation(rhs, lhs), addr);
	}

	public void substitute(HashMap<Equation, Token> t1,
			HashMap<Equation, Token> s1, Map<Equation, Token> t,
			Map<Equation, Token> s) {
		Token var = ((TypeVariable) lhs).variable;
		for (Equation e : t1.keySet())
			t.put(new Equation(e.lhs.substitute(var, rhs), e.rhs.substitute(
					var, rhs)), t1.get(e));
		for (Equation e : s1.keySet())
			s.put(new Equation(e.lhs.substitute(var, rhs), e.rhs.substitute(
					var, rhs)), s1.get(e));
		if (t1.containsKey(this)) {
			s.put(this, t1.get(this));
			return;
		}
		if (s1.containsKey(this)) {
			s.put(this, s1.get(this));
			return;
		}
	}

	@Override
	public String toString() {
		return lhs.toString() + " / " + rhs.toString();
	}
}
