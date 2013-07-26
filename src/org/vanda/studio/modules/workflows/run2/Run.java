package org.vanda.studio.modules.workflows.run2;

import java.util.Date;

import org.vanda.execution.model.Runables.RunEvent;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.run2.Runs.RunState;
import org.vanda.studio.modules.workflows.run2.Runs.RunTransitions;
import org.vanda.studio.modules.workflows.run2.Runs.StateCancelled;
import org.vanda.studio.modules.workflows.run2.Runs.StateInit;
import org.vanda.studio.modules.workflows.run2.Runs.StateDone;
import org.vanda.util.Observer;

public class Run implements RunTransitions {
	private Date date;
	private String id;
	private Observer<RunEvent> obs;
	private RunState state;
	private Application app;

	public Run(Application app, Observer<RunEvent> obs, String id) {
		date = new Date();
		this.id = id;
		this.obs = obs;
		this.app = app;
		state = new StateInit(this);
		state.process();
	}

	public String toString() {
		return state.getString(date);
	}

	@Override
	public void doCancel() {
		state = new StateCancelled();
		state.process();
	}

	public void cancel() {
		state.cancel();
	}

	@Override
	public void doFinish() {
		state = new StateDone();
		state.process();
	}

	@Override
	public void doRun() {
		state = new StateRunning(obs, id, app, this);
		state.process();
	}
	
	public void run() {
		state.run();
	}

}
