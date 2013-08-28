package org.vanda.execution.model;

import org.vanda.execution.model.Runables.RunState;
import org.vanda.execution.model.Runables.RunStateVisitor;

public class StateDone implements RunState {

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
		return true;
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
		return false;
	}
	
	@Override
	public void progress(Runable rt, int progress) {
		// do nothing
	}
	
	@Override
	public void run(Runable rt) {
		// do nothing
	}

	@Override
	public void visit(RunStateVisitor runStateVisitor) {
		runStateVisitor.done();
	}

}
