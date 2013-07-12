package org.vanda.execution.model;

import org.vanda.execution.model.Runables.RunState;
import org.vanda.execution.model.Runables.RunStateVisitor;

public class StateReady implements RunState {

	@Override
	public void cancel(Runable rt) {
		rt.doCancel();
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
		return false;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public void run(Runable rt) {
		rt.doRun();
	}

	@Override
	public void visit(RunStateVisitor runStateVisitor) {
		runStateVisitor.ready();
	}

}
