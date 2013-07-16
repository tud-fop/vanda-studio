package org.vanda.execution.model;

import java.util.Observable;

import org.vanda.execution.model.Runables.DefaultRunEventListener;
import org.vanda.execution.model.Runables.RunEvent;
import org.vanda.execution.model.Runables.RunEventListener;
import org.vanda.execution.model.Runables.RunState;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job;

public class ExecutableJob extends Job implements Runable {
	private RunEventListener listener;
	private RunState state;
	private String toolPrefix;

	public ExecutableJob(Job ji) {
		this(ji, "");
	}

	public ExecutableJob(Job ji, String toolPrefix) {
		super(ji.getElement(), true);
		this.setDimensions(new double[] { ji.getX(), ji.getY(), ji.getWidth(),
				ji.getHeight() });
		this.state = new StateReady();
		this.toolPrefix = toolPrefix;
		this.listener = new DefaultRunEventListener(this);
	}

	@Override
	public void doCancel() {
		// TODO cancel execution
		state = new StateCancelled();
		propertyChanged(getElement());
	}

	@Override
	public void doFinish() {
		state = new StateDone();
		propertyChanged(getElement());
	}

	@Override
	public void doRun() {
		state = new StateRunning();
		propertyChanged(getElement());
	}

	@Override
	public String getID() {
		return toolPrefix;
	}

	@Override
	public RunState getState() {
		return state;
	}

	public String getToolPrefix() {
		return toolPrefix;
	}

	public ValuedLocation getValuedBinding(Port p) {
		return (ValuedLocation) bindings.get(p);
	}

	@Override
	public void registerRunEventListener(MultiplexObserver<RunEvent> observable) {
		observable.addObserver(new Observer<RunEvent>() {
			@Override
			public void notify(RunEvent event) {
				event.doNotify(listener);
			}
		});
	}

	public void setToolPrefix(String toolPrefix) {
		this.toolPrefix = toolPrefix;
	}

	public void shift(double x, double y) {
		setDimensions(new double[] { getX() + x, getY() + y, getWidth(),
				getHeight() });
		if (this.getObservable() != null) 
			propertyChanged(getElement());
	}
}
