package org.vanda.studio.model.types;

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

	public void substitute(Map<Equation, Token> source1,
			Map<Equation, Token> source2, Map<Equation, Token> target1,
			Map<Equation, Token> target2, Token addr) {
		Token var = ((TypeVariable) lhs).variable;
		for (Equation e : source1.keySet())
			target1.put(
					new Equation(e.lhs.substitute(var, rhs), e.rhs.substitute(
							var, rhs)), source1.get(e));
		for (Equation e : source2.keySet())
			target2.put(
					new Equation(e.lhs.substitute(var, rhs), e.rhs.substitute(
							var, rhs)), source2.get(e));
		target2.put(this, addr);
	}

	@Override
	public String toString() {
		return lhs.toString() + " / " + rhs.toString();
	}
}
