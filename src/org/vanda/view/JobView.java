package org.vanda.view;

import org.vanda.execution.model.RunStates.RunEvent;
import org.vanda.execution.model.RunStates.RunEventListener;
import org.vanda.execution.model.RunStates.*;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.view.Views.SelectionObject;
import org.vanda.view.Views.*;
import org.vanda.workflows.hyper.Job;

public class JobView extends AbstractView<Job> implements Observer<RunEvent> {

	private final MultiplexObserver<RunEvent> rsObserver = new MultiplexObserver<RunEvent>();
	private RunEvent runState = new RunStateReady();
	private int progress = 0;
	
	public JobView(ViewListener<AbstractView<?>> listener) {
		super(listener);
	}
	
	public int getRunProgress() {
		return progress;
	}
	
	public Observable<RunEvent> getRsObservable() {
		return rsObserver;
	}

	@Override
	public SelectionObject createSelectionObject(Job t) {
		return new JobSelectionObject(t);
	}

	public void visit(RunEventListener rsv) {
		runState.doNotify(rsv);
	}

	@Override
	public void notify(RunEvent event) {
		runState = event;
		rsObserver.notify(event);
	}
}
