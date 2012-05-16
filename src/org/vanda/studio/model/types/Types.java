package org.vanda.studio.model.types;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class Types {
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
}
