package org.vanda.studio.model.types;

import java.util.Set;

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

	public void decompose(Set<Equation> target) {
		CompositeType l = (CompositeType) lhs;
		CompositeType r = (CompositeType) rhs;
		for (int i = 0; i < l.children.size(); i++) {
			target.add(new Equation(l.children.get(i), r.children.get(i)));
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

	public void flip(Set<Equation> target) {
		target.add(new Equation(rhs, lhs));
	}

	public void substitute(Set<Equation> source1, Set<Equation> source2,
			Set<Equation> target1, Set<Equation> target2) {
		Object var = ((TypeVariable) lhs).variable;
		for (Equation e : source1)
			target1.add(new Equation(e.lhs.substitute(var, rhs), e.rhs
					.substitute(var, rhs)));
		for (Equation e : source2)
			target2.add(new Equation(e.lhs.substitute(var, rhs), e.rhs
					.substitute(var, rhs)));
		target2.add(this);
	}

	@Override
	public String toString() {
		return lhs.toString() + " / " + rhs.toString();
	}
}
