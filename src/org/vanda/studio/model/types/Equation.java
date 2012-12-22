package org.vanda.studio.model.types;

import java.util.List;
import java.util.Map;

import org.vanda.studio.util.Pair;
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
		return (lhs.canDecompose() && rhs.canDecompose());
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

	public void decompose(Map<Equation, Pair<Token, Integer>> t,
			Pair<Token, Integer> addr) {
		Pair<String, List<Type>> ld = lhs.decompose();
		Pair<String, List<Type>> rd = rhs.decompose();
		for (int i = 0; i < ld.snd.size(); i++) {
			t.put(new Equation(ld.snd.get(i), rd.snd.get(i)), addr);
		}
	}

	public boolean failsDecomposeCheck() {
		Pair<String, List<Type>> ld = lhs.decompose();
		Pair<String, List<Type>> rd = rhs.decompose();
		return !ld.fst.equals(rd.fst) || ld.snd.size() != rd.snd.size();
	}

	public boolean failsOccursCheck() {
		return lhs.failsOccursCheck(rhs);
	}

	public void flip(Map<Equation, Pair<Token, Integer>> t,
			Pair<Token, Integer> addr) {
		t.put(new Equation(rhs, lhs), addr);
	}

	public Equation subst(Token var, Type type) {
		return new Equation(lhs.subst(var, type), rhs.subst(var, type));
	}

	public void substitute(Map<Equation, Pair<Token, Integer>> source,
			Map<Equation, Pair<Token, Integer>> target) {
		Token var = ((TypeVariable) lhs).variable;
		for (Equation e : source.keySet())
			target.put(e.subst(var, rhs), source.get(e));
	}

	@Override
	public String toString() {
		return lhs.toString() + " / " + rhs.toString();
	}
}
