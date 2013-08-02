package org.vanda.view;

import org.vanda.execution.model.Runable;
import org.vanda.execution.model.StateCancelled;
import org.vanda.execution.model.StateDone;
import org.vanda.execution.model.StateReady;
import org.vanda.execution.model.StateRunning;
import org.vanda.execution.model.Runables.RunState;
import org.vanda.workflows.hyper.Job;

public class JobView extends AbstractView implements Runable {

	private RunState runState = new StateReady();

	@Override
	public void remove(View view) {
		for (Job j : view.jobs.keySet())
			if (view.getJobView(j) == this) {
				view.getWorkflow().removeChild(j);
				break;
			}
	}

	@Override
	public void visit(SelectionVisitor sv, View view) {
		for (Job j : view.jobs.keySet())
			if (view.getJobView(j) == this) {
				sv.visitJob(view.getWorkflow(), j);
				break;
			}
	}

	@Override
	public void doCancel() {
		// TODO cancel execution
		RunState oldState = runState;
		runState = new StateCancelled();
		getObservable().notify(new RunStateTransitionEvent<AbstractView>(this, oldState, runState));
	}

	@Override
	public void doFinish() {
		RunState oldState = runState;
		runState = new StateDone();
		getObservable().notify(new RunStateTransitionEvent<AbstractView>(this, oldState, runState));
	}

	@Override
	public void doRun() {
		RunState oldState = runState;
		runState = new StateRunning();
		getObservable().notify(new RunStateTransitionEvent<AbstractView>(this, oldState, runState));
	}

	@Override
	public RunState getState() {
		return runState;
	}


}
