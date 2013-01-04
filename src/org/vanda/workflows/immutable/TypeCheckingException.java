package org.vanda.workflows.immutable;

import java.util.List;
import java.util.Set;

import org.vanda.util.Pair;
import org.vanda.workflows.immutable.TypeChecker.EqInfo;

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
