package org.vanda.workflows.hyper;

import java.util.List;
import java.util.Set;

import org.vanda.util.Pair;

public final class TypeCheckingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public List<Pair<String, Set<ConnectionKey>>> errors;
	
	public TypeCheckingException(List<Pair<String, Set<ConnectionKey>>> errors) {
		super("Unification failed");
		this.errors = errors;
	}

	public List<Pair<String, Set<ConnectionKey>>> getErrors() {
		return errors;
	}

}
