package org.vanda.fragment.model;

import java.util.Collections;
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

	// FIXME ugly, provisional, for jobSorting
	private boolean executable;

	public SyntaxAnalysis(MutableWorkflow hwf, boolean executable) {
		this.hwf = hwf;
		this.executable = executable;
		syntaxChangedObservable = new MultiplexObserver<SyntaxAnalysis>();
		try {
			checkWorkflow();
		} catch (Exception e) {
			// do nothing
		}
	}
	
	public SyntaxAnalysis(MutableWorkflow hwf) {
		this(hwf, false);
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
		if (executable)
			sorted = hwf.getChildren().toArray(new Job[hwf.getChildren().size()]);
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
