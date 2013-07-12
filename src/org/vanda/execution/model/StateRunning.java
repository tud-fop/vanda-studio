package org.vanda.execution.model;

import org.vanda.execution.model.Runables.RunState;
import org.vanda.execution.model.Runables.RunStateVisitor;

public class StateRunning implements RunState {

	@Override
	public void cancel(Runable rt) {
		rt.doCancel();
	}

	@Override
	public void finish(Runable rt) {
		rt.doFinish();
	}

	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	public boolean isErroneous() {
		return false;
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public void run(Runable rt) {
		// do nothing
	}

	@Override
	public void visit(RunStateVisitor runStateVisitor) {
		runStateVisitor.running();
	}

}
