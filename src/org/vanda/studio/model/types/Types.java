package org.vanda.studio.model.types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource;
import org.vanda.studio.util.TokenSource.Token;
import org.vanda.studio.util.UnificationException;

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
		Equation eq = new Equation(t1
				.rename(rename1), t2.rename(rename2));
		try {
			Map<Equation, Pair<Token, Integer>> m = new HashMap<Equation, Pair<Token, Integer>>();
			m.put(eq, null);
			unify(m);
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
	public static void unify(Map<Equation, Pair<Token, Integer>> s) throws Exception {
		Map<Equation, Pair<Token, Integer>> t = new HashMap<Equation, Pair<Token, Integer>>(s);
		s.clear();
		Iterator<Equation> it = t.keySet().iterator();
		while (it.hasNext()) {
			// System.out.println(t);
			Equation e = it.next();
			Pair<Token, Integer> addr = t.get(e);
			t.remove(e);
			// System.out.println(e);
			if (e.canDecompose()) {
				if (e.failsDecomposeCheck())
					throw new UnificationException(addr);
				// System.out.println("decompose");
				e.decompose(t, t.get(e));
			} else if (e.canFlip()) {
				// System.out.println("flip");
				e.flip(t, t.get(e));
			} else if (e.canEliminate()) {
				// System.out.println("eliminate");
				// do nothing (== elimination)
			} else if (e.canSubstitute()) {
				// System.out.println("substitute");
				if (e.failsOccursCheck())
					throw new Exception("Occurs check fail");
				HashMap<Equation, Pair<Token, Integer>> s1 = new HashMap<Equation, Pair<Token, Integer>>(s);
				HashMap<Equation, Pair<Token, Integer>> t1 = new HashMap<Equation, Pair<Token, Integer>>(t);
				s.clear();
				t.clear();
				e.substitute(t1, t);
				e.substitute(s1, s);
				s.put(e, addr);
				// System.out.println(s);
			} else {
				s.put(e, addr);
			}
			it = t.keySet().iterator();
		}
	}

	public static final Type genericType = new TypeVariable(TokenSource.getToken(0));
	public static final Type haskellType = new CompositeType("haskell");
	public static final Type shellType = new CompositeType("shell");
	public static final Type undefined = new CompositeType("bottom");
}
