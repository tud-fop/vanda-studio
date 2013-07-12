package org.vanda.execution.model;

import org.vanda.execution.model.Runables.RunState;
import org.vanda.execution.model.Runables.RunStateVisitor;

public class StateCancelled implements RunState {

	@Override
	public void cancel(Runable rt) {
		// do nothing
	}

	@Override
	public void finish(Runable rt) {
		// do nothing
	}

	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	public boolean isErroneous() {
		return true;
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public void run(Runable rt) {
		// do nothing
	}

	@Override
	public void visit(RunStateVisitor runStateVisitor) {
		runStateVisitor.cancelled();
	}
	
	
	
}
