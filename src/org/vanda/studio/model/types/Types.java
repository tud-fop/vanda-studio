package org.vanda.studio.model.types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
			Map<Equation, Token> m = new HashMap<Equation, Token>();
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
	public static void unify(Map<Equation, Token> s) throws Exception {
		Map<Equation, Token> t = new HashMap<Equation, Token>(s);
		s.clear();
		Iterator<Equation> it = t.keySet().iterator();
		while (it.hasNext()) {
			// System.out.println(t);
			Equation e = it.next();
			Token addr = t.get(e);
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
				HashMap<Equation, Token> s1 = new HashMap<Equation, Token>(s);
				HashMap<Equation, Token> t1 = new HashMap<Equation, Token>(t);
				s.clear();
				t.clear();
				e.substitute(t1, s1, t, s);
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
