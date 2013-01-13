package org.vanda.types;

import java.util.List;

import org.vanda.util.Pair;
import org.vanda.util.TokenSource.Token;

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
		Type newlhs = lhs.subst(var, type);
		Type newrhs = rhs.subst(var, type);
		Equation<X> result = null;
		if (lhs != newlhs || rhs != newrhs)
			result = new Equation<X>(newlhs, newrhs, anc);
		else
			result = this;
		return result;
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
		StringBuilder sb = new StringBuilder();
		sb.append(lhs);
		sb.append(" / ");
		sb.append(rhs);
		if (ancillary != null) {
			sb.append(" # ");
			sb.append(ancillary);
		}
		return sb.toString();
	}
}
