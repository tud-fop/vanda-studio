package org.vanda.studio.modules.workflows.run;

import java.util.Date;

import org.vanda.execution.model.RunStates.*;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.run.Runs.*;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observer;

public class Run implements RunEventListener, RunTransitions {
	private final Date date;
	private final String id;
	private final MultiplexObserver<RunEvent> observable1 = new MultiplexObserver<RunEvent>();
	private final MultiplexObserver<RunEventId> observable;
	private RunState state;
	private Application app;

	public Run(Application app, Observer<RunEventId> obs, String id) {
		date = new Date();
		this.id = id;
		observable = new MultiplexObserver<RunEventId>();
		observable.addObserver(obs);
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
		observable1.notify(new RunStateCancelled());
	}

	@Override
	public void doFinish() {
		state = new StateDone();
		state.process();
		observable1.notify(new RunStateDone());
	}
	
	public String getId() {
		return id;
	}
	
	public MultiplexObserver<RunEvent> getObservable() {
		return observable1;
	}
	
	public MultiplexObserver<RunEventId> getObservableId() {
		return observable;
	}

	@Override
	public void doRun() {
		state = new StateRunning(observable, id, app, this);
		state.process();
		observable1.notify(new RunStateRunning());
	}
	
	@Override
	public void done() {
		// do nothing
	}

	public void visit(RunEventListener rsv) {
		state.visit(rsv);
	}

	@Override
	public void progress(int progress) {
		// do nothing
	}

	@Override
	public void cancelled() {
		state.cancel();
	}

	@Override
	public void ready() {
		// ignore
	}

	@Override
	public void running() {
		state.run();
	}
}
