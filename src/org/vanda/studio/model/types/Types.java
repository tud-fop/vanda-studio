package org.vanda.studio.model.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.vanda.studio.util.TokenSource;
import org.vanda.studio.util.TokenSource.Token;

public final class Types {
	
	/**
	 * Checks whether two types can be unified. Uses fresh copies to avoid
	 * name clashes.
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
		Set<Equation> eqs = Collections.singleton(new Equation(t1
				.rename(rename1), t2.rename(rename2)));
		try {
			unify(new HashSet<Equation>(eqs));
			return true;
		}
		catch (Exception e) {
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
	public static void unify(Set<Equation> s) throws Exception {
		Set<Equation> t = new HashSet<Equation>(s);
		s.clear();
		Iterator<Equation> it = t.iterator();
		while (it.hasNext()) {
			// System.out.println(t);
			Equation e = it.next();
			t.remove(e);
			// System.out.println(e);
			if (e.canDecompose()) {
				if (e.failsDecomposeCheck())
					throw new Exception("Unification failed");
				// System.out.println("decompose");
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
					throw new Exception("Occurs check fail");
				HashSet<Equation> s1 = new HashSet<Equation>(s);
				HashSet<Equation> t1 = new HashSet<Equation>(t);
				s.clear();
				t.clear();
				e.substitute(t1, s1, t, s);
				// System.out.println(s);
			} else {
				s.add(e);
			}
			it = t.iterator();
		}
	}

	public static final Type genericType = new TypeVariable(TokenSource.getToken(0));
	public static final Type haskellType = new CompositeType("haskell");
	public static final Type shellType = new CompositeType("shell");
}
