package org.vanda.studio.model.immutable;

import java.util.List;
import java.util.Set;

import org.vanda.studio.model.immutable.TypeChecker.EqInfo;
import org.vanda.studio.util.Pair;

public final class TypeCheckingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public List<Pair<String, Set<EqInfo>>> errors;
	
	public TypeCheckingException(List<Pair<String, Set<EqInfo>>> errors) {
		super("Unification failed");
		this.errors = errors;
	}

	public List<Pair<String, Set<EqInfo>>> getErrors() {
		return errors;
	}

}
