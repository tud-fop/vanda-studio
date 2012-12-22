package org.vanda.studio.model.types;

import java.util.List;

import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource.Token;

public final class Equation<X> {

	public final Type lhs;
	public final Type rhs;
	public final X ancillary;

	public Equation(Type lhs, Type rhs, X ancillary) {
		this.lhs = lhs;
		this.rhs = rhs;
		this.ancillary = ancillary;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Equation) {
			Equation<?> o = (Equation<?>) other;
			return (lhs.equals(o.lhs) && rhs.equals(o.rhs))
					&& ancillary.equals(o.ancillary);
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

	public void decompose(List<Equation<X>> t) {
		Pair<String, List<Type>> ld = lhs.decompose();
		Pair<String, List<Type>> rd = rhs.decompose();
		for (int i = 0; i < ld.snd.size(); i++) {
			t.add(new Equation<X>(ld.snd.get(i), rd.snd.get(i), ancillary));
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

	public void flip(List<Equation<X>> t) {
		t.add(new Equation<X>(rhs, lhs, ancillary));
	}

	public Equation<X> subst(Token var, Type type, X anc) {
		return new Equation<X>(lhs.subst(var, type), rhs.subst(var, type), anc);
	}

	public void substitute(List<Equation<X>> source, List<Equation<X>> target,
			MergeFunction<X> mf) {
		Token var = ((TypeVariable) lhs).variable;
		for (Equation<X> e : source)
			target.add(e.subst(var, rhs,
					mf == null ? null : mf.merge(ancillary, e.ancillary)));
	}

	@Override
	public String toString() {
		return lhs.toString() + " / " + rhs.toString();
	}
}
