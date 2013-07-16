package org.vanda.studio.modules.workflows.run2;

import java.util.Date;

import javax.swing.SwingWorker;

import org.vanda.execution.model.ExecutableWorkflow;
import org.vanda.fragment.model.Fragment;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.run2.Runs.RunState;
import org.vanda.studio.modules.workflows.run2.Runs.RunTransitions;
import org.vanda.studio.modules.workflows.run2.Runs.StateCancelled;
import org.vanda.studio.modules.workflows.run2.Runs.StateInit;
import org.vanda.studio.modules.workflows.run2.Runs.StateDone;

public class Run extends SwingWorker<String, String> implements RunTransitions {
	private Date date;
	private Fragment frag;
	private ExecutableWorkflow ew;
	private RunState state = new StateInit();
	private Application app;

	public Run(Application app, ExecutableWorkflow ew, Fragment fragment) {
		date = new Date();
		frag = fragment;
		this.ew = ew;
		this.app = app;
	}

	public String toString() {
		return state.getString(date);
	}

	@Override
	public void doCancel() {
		state = new StateCancelled();
	}

	public void cancel() {
		super.cancel(true);
		state.cancel(this);
	}

	@Override
	public void doFinish() {
		state = new StateDone();
	}

	@Override
	public void doRun() {
		state = new StateRunning(ew, frag, app);
		state.finish(this);
	}

	@Override
	protected String doInBackground() {
		System.out.println("Do in Background");
		state.run(this);
		return null;
	}

}
