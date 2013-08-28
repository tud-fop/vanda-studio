package org.vanda.fragment.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.vanda.types.Type;
import org.vanda.util.MultiplexObserver;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.TypeChecker;
import org.vanda.workflows.hyper.TypeCheckingException;

public class SyntaxAnalysis {
	private MutableWorkflow hwf;
	private Map<Object, Type> types = Collections.emptyMap();
	private Type fragmentType = null;
	private MultiplexObserver<SyntaxAnalysis> syntaxChangedObservable;
	protected Job[] sorted = null;
	private final Comparator<Job> priorities;

	public SyntaxAnalysis(MutableWorkflow hwf, Comparator<Job> priorities) {
		this.hwf = hwf;
		this.priorities = priorities;
		syntaxChangedObservable = new MultiplexObserver<SyntaxAnalysis>();
		try {
			checkWorkflow();
		} catch (Exception e) {
			// do nothing
		}
	}
	
	public SyntaxAnalysis(MutableWorkflow hwf) {
		this(hwf, null);
	}

	public void typeCheck() throws TypeCheckingException {
		TypeChecker tc = new TypeChecker();
		hwf.typeCheck(tc);
		tc.check();
		types = tc.getTypes();
		fragmentType = tc.getFragmentType();

	}

	public void checkWorkflow() throws TypeCheckingException, Exception {
		sorted = null;
		typeCheck();
		if (priorities != null)
			sorted = hwf.getSorted(priorities);
		else
			sorted = hwf.getSorted();
		syntaxChangedObservable.notify(this);
	}

	public Job[] getSorted() {
		return sorted;
	}

	public Type getFragmentType() {
		return fragmentType;
	}

	public Type getType(Object variable) {
		return types.get(variable);
	}

	public MultiplexObserver<SyntaxAnalysis> getSyntaxChangedObservable() {
		return syntaxChangedObservable;
	}
}
