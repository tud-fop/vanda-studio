package org.vanda.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.vanda.util.Pair;
import org.vanda.util.TokenSource;
import org.vanda.util.TokenSource.Token;

public final class Types {

	/**
	 * Checks whether two types can be unified. Uses fresh copies to avoid name
	 * clashes.
	 * 
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static boolean canUnify(Type t1, Type t2) {
		if (t1 == t2) // also for null?
			return true;
		if (t1 == null || t2 == null)
			return false;
		TokenSource freshSource = new TokenSource();
		HashMap<Token, Token> rename1 = new HashMap<Token, Token>();
		HashMap<Token, Token> rename2 = new HashMap<Token, Token>();
		t1.freshMap(freshSource, rename1);
		t2.freshMap(freshSource, rename2);
		Equation<Object> eq = new Equation<Object>(t1.rename(rename1),
				t2.rename(rename2), null);
		try {
			List<Equation<Object>> l = new ArrayList<Equation<Object>>();
			l.add(eq);
			unify(l, null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Unification algorithm. Modifies s in-place.
	 * 
	 * @param s
	 * @throws Exception
	 *             if there is no mgu
	 */
	public static <X> List<Pair<String, X>> unify(List<Equation<X>> s,
			MergeFunction<X> mf) {
		LinkedList<Equation<X>> t = new LinkedList<Equation<X>>(s);
		LinkedList<Pair<String, X>> errors = new LinkedList<Pair<String, X>>();
		s.clear();
		while (!t.isEmpty()) {
			// System.err.println(t);
			Equation<X> e = t.poll();
			// System.out.println(e);
			if (e.canDecompose()) {
				// System.out.println("decompose");
				if (e.failsDecomposeCheck()) {
					// throw new UnificationException(addr);
					errors.add(new Pair<String, X>("unification error",
							e.ancillary));
				} else
					e.decompose(t);
			} else if (e.canFlip()) {
				// System.out.println("flip");
				e.flip(t);
			} else if (e.canEliminate()) {
				// System.out.println("eliminate");
				// do nothing (== elimination)
			} else if (e.canSubstitute()) {
				// System.out.println("substitute");
				if (e.failsOccursCheck())
					// throw new Exception("Occurs check fail");
					errors.add(new Pair<String, X>("occurs check error",
							e.ancillary));
				else {
					List<Equation<X>> s1 = new ArrayList<Equation<X>>(s);
					List<Equation<X>> t1 = new ArrayList<Equation<X>>(t);
					s.clear();
					t.clear();
					e.substitute(t1, t, mf);
					e.substitute(s1, s, mf);
					s.add(e);
				}
			} else {
				s.add(e);
			}
		}
		if (!errors.isEmpty())
			System.err.println(errors);
		return errors;
	}

	public static final Type genericType = new TypeVariable(
			TokenSource.getToken(0));
	public static final Type haskellType = new CompositeType("haskell");
	public static final Type shellType = new CompositeType("shell");
	public static final Type undefined = new CompositeType("bottom");
}
