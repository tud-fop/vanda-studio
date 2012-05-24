package org.vanda.studio.model.hyper;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.hyper.Job.JobEvent;
import org.vanda.studio.model.hyper.Job.JobListener;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.TokenSource.Token;

public final class MutableWorkflow extends DrecksWorkflow implements Cloneable,
		JobListener {

	public static interface WorkflowListener {
		void inputPortAdded(MutableWorkflow mwf, int index);

		void inputPortRemoved(MutableWorkflow mwf, int index);

		void outputPortAdded(MutableWorkflow mwf, int index);

		void outputPortRemoved(MutableWorkflow mwf, int index);

		void propertyChanged(MutableWorkflow mwf);
	}

	public static interface WorkflowEvent {
		void doNotify(WorkflowListener wl);
	}

	public static interface WorkflowChildListener {
		void childAdded(MutableWorkflow mwf, Job j);

		void childModified(MutableWorkflow mwf, Job j);

		void childRemoved(MutableWorkflow mwf, Job j);

		void connectionAdded(MutableWorkflow mwf, Connection cc);

		void connectionRemoved(MutableWorkflow mwf, Connection cc);

		void inputPortAdded(MutableWorkflow mwf, Job j, int index);

		void inputPortRemoved(MutableWorkflow mwf, Job j, int index);

		void outputPortAdded(MutableWorkflow mwf, Job j, int index);

		void outputPortRemoved(MutableWorkflow mwf, Job j, int index);
	}

	public static interface WorkflowChildEvent {
		void doNotify(WorkflowChildListener wcl);
	}

	private final MultiplexObserver<WorkflowEvent> observable;
	private final MultiplexObserver<WorkflowChildEvent> childObservable;
	private final Observer<JobEvent> childObserver;

	{
		childObserver = new Observer<JobEvent>() {
			@Override
			public void notify(JobEvent event) {
				event.doNotify(MutableWorkflow.this);
			}
		};
	}

	public MutableWorkflow(String name) {
		super(name);
		observable = new MultiplexObserver<MutableWorkflow.WorkflowEvent>();
		childObservable = new MultiplexObserver<MutableWorkflow.WorkflowChildEvent>();
	}

	public MutableWorkflow(MutableWorkflow hyperWorkflow)
			throws CloneNotSupportedException {
		super(hyperWorkflow);
		observable = hyperWorkflow.observable.clone();
		childObservable = hyperWorkflow.childObservable.clone();
	}

	@Override
	public MutableWorkflow clone() throws CloneNotSupportedException {
		return new MutableWorkflow(this);
	}

	public Token addChild(Job job) {
		assert (job.address == null);
		job.address = childAddressSource.makeToken();
		DJobInfo ji = new DJobInfo(this, job);
		if (job.address.intValue() < children.size())
			children.set(job.address.intValue(), ji);
		else {
			assert (job.address.intValue() == children.size());
			children.add(ji);
		}
		bind(job);
		if (job.isInputPort()) {
			Token t = inputPortSource.makeToken();
			if (t.intValue() < inputPorts.size())
				inputPorts.set(t.intValue(), job.address);
			else {
				assert (t.intValue() == inputPorts.size());
				inputPorts.add(job.address);
			}
			ji.portNumber = t;
			observable.notify(new Workflows.InputPortAddedEvent(this, t
					.intValue()));
		} else if (job.isOutputPort()) {
			Token t = outputPortSource.makeToken();
			if (t.intValue() < outputPorts.size())
				outputPorts.set(t.intValue(), job.address);
			else {
				assert (t.intValue() == outputPorts.size());
				outputPorts.add(job.address);
			}
			ji.portNumber = t;
			observable.notify(new Workflows.OutputPortAddedEvent(this,
					getOutputPorts().indexOf(job.getInputPorts().get(0))));
		}
		childObservable.notify(new Workflows.ChildAddedEvent(this, job));
		return job.address;
	}

	public Token addConnection(Connection cc) {
		assert (cc.address == null);
		DJobInfo sji = children.get(cc.source.intValue());
		DJobInfo tji = children.get(cc.target.intValue());
		assert (sji.job.getOutputPorts().get(cc.sourcePort) != null);
		assert (tji.job.getInputPorts().get(cc.targetPort) != null);
		if (tji.inputs.get(cc.targetPort) != null)
			throw new RuntimeException("!!!"); // FIXME better exception
		Token tok = sji.outputs.get(cc.sourcePort);
		DConnInfo ci = new DConnInfo(tok, cc);
		cc.address = connectionAddressSource.makeToken();
		tji.inputs.set(cc.targetPort, tok);
		tji.inputsBlocked++;
		if (cc.address.intValue() < connections.size())
			connections.set(cc.address.intValue(), ci);
		else {
			assert (cc.address.intValue() == connections.size());
			connections.add(ci);
		}
		sji.outCount++;
		childObservable.notify(new Workflows.ConnectionAddedEvent(this, cc));
		return cc.address;
	}

	public Observable<WorkflowEvent> getObservable() {
		return observable;
	}

	public Observable<WorkflowChildEvent> getChildObservable() {
		return childObservable;
	}

	public Token getVariable(Token source, int sourcePort) {
		DJobInfo ji = children.get(source.intValue());
		if (ji != null && 0 <= sourcePort && sourcePort < ji.outputs.size()) {
			return ji.outputs.get(sourcePort);
		} else
			return null;
	}

	public Token getVariable(Token address) {
		DConnInfo ci = connections.get(address.intValue());
		if (ci != null)
			return ci.variable;
		else
			return null;
	}

	public void removeChild(Token address) {
		DJobInfo ji = children.get(address.intValue());
		if (ji == null)
			return;
		if (ji.job.isInputPort()) {
			inputPorts.set(ji.portNumber.intValue(), null);
			inputPortSource.recycleToken(ji.portNumber);
			observable.notify(new Workflows.InputPortRemovedEvent(this,
					ji.portNumber.intValue()));
		} else if (ji.job.isOutputPort()) {
			outputPorts.set(ji.portNumber.intValue(), null);
			outputPortSource.recycleToken(ji.portNumber);
			observable.notify(new Workflows.OutputPortRemovedEvent(this,
					ji.portNumber.intValue()));
		}
		for (int i = 0; i < connections.size(); i++) {
			DConnInfo ci = connections.get(i);
			if (ci != null) {
				if (ci.cc.source == address || ci.cc.target == address)
					removeConnection(ci.cc.address);
			}
		}
		for (int i = 0; i < ji.outputs.size(); i++) {
			variableSource.recycleToken(ji.outputs.get(i));
		}
		unbind(ji.job);
		children.set(ji.job.address.intValue(), null);
		childObservable.notify(new Workflows.ChildRemovedEvent(this, ji.job));
		ji.job.address = null;
		childAddressSource.recycleToken(address);
	}

	public void removeConnection(Token address) {
		DConnInfo ci = connections.get(address.intValue());
		if (ci != null) {
			DJobInfo sji = children.get(ci.cc.source.intValue());
			DJobInfo tji = children.get(ci.cc.target.intValue());
			// assert (sji.outputs.get(sourcePort) == tji.inputs.get(ci.port));
			tji.inputs.set(ci.cc.targetPort, null);
			tji.inputsBlocked--;
			sji.outCount--;
			connections.set(address.intValue(), null);
			childObservable.notify(new Workflows.ConnectionRemovedEvent(this,
					ci.cc));
			ci.cc.address = null;
			connectionAddressSource.recycleToken(address);
		}
	}

	public MutableWorkflow dereference(ListIterator<Token> address) {
		assert (address != null);
		if (address.hasNext()) {
			DJobInfo ji = children.get(address.next().intValue());
			if (ji != null)
				return ji.job.dereference(address);
			else
				return null;
		} else
			return this;
	}

	public List<Connection> getConnections() {
		// only for putting existing HyperGraphs into the GUI
		LinkedList<Connection> conn = new LinkedList<Connection>();
		for (DConnInfo ci : connections) {
			if (ci != null)
				conn.add(ci.cc);
		}
		return conn;
	}

	public Job getChild(Token address) {
		DJobInfo ji = children.get(address.intValue());
		if (ji != null)
			return ji.job;
		else
			return null;
	}

	public Connection getConnection(Token address) {
		DConnInfo ci = connections.get(address.intValue());
		if (ci != null)
			return ci.cc;
		else
			return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (!name.equals(this.name)) {
			this.name = name;
			observable.notify(new Workflows.PropertyChangedEvent(this));
		}
	}

	private void bind(Job job) {
		register(job.getObservable(), childObserver);
	}

	private void unbind(Job job) {
		unregister(job.getObservable(), childObserver);
	}

	private static <T> void register(Observable<T> obs, Observer<T> o) {
		if (obs != null)
			obs.addObserver(o);
	}

	private static <T> void unregister(Observable<T> obs, Observer<T> o) {
		if (obs != null)
			obs.removeObserver(o);
	}

	/**
	 * Call this after deserialization.
	 */
	public void rebind() {
		for (DJobInfo ji : children)
			if (ji != null) {
				ji.job.rebind();
				bind(ji.job);
			}
	}

	public void visitAll(JobVisitor v) {
		for (DJobInfo ji : children)
			if (ji != null)
				ji.job.visit(v);
	}

	@Override
	public void inputPortAdded(Job j, int index) {
		DJobInfo ji = children.get(j.address.intValue());
		if (index >= ji.inputs.size()) {
			assert (index == ji.inputs.size());
			ji.inputs.add(null);
		} else
			assert (ji.inputs.get(index) == null);
		childObservable.notify(new Workflows.ChildInputPortAddedEvent(this, j,
				index));
	}

	@Override
	public void inputPortRemoved(Job j, int index) {
		DJobInfo ji = children.get(j.address.intValue());
		// remove connections to this port!
		for (int i = 0; i < connections.size(); i++) {
			DConnInfo ci = connections.get(i);
			if (ci != null) {
				if (ci.cc.target == j.address && ci.cc.targetPort == index)
					removeConnection(ci.cc.address);
			}
		}
		ji.inputs.set(index, null);
		childObservable.notify(new Workflows.ChildInputPortRemovedEvent(this,
				j, index));
	}

	@Override
	public void outputPortAdded(Job j, int index) {
		DJobInfo ji = children.get(j.address.intValue());
		if (index >= ji.outputs.size()) {
			assert (index == ji.outputs.size());
			ji.outputs.add(null);
		} else
			assert (ji.outputs.get(index) == null);
		ji.outputs.set(index, variableSource.makeToken());
		childObservable.notify(new Workflows.ChildOutputPortAddedEvent(this, j,
				index));
	}

	@Override
	public void outputPortRemoved(Job j, int index) {
		DJobInfo ji = children.get(j.address.intValue());
		Token var = ji.outputs.set(index, null);
		variableSource.recycleToken(var);
		// remove connections from this port
		for (int i = 0; i < connections.size(); i++) {
			DConnInfo ci = connections.get(i);
			if (ci != null) {
				if (ci.cc.source == j.address && ci.cc.sourcePort == index)
					removeConnection(ci.cc.address);
			}
		}
		childObservable.notify(new Workflows.ChildOutputPortRemovedEvent(this,
				j, index));
	}

	@Override
	public void propertyChanged(Job j) {
		childObservable.notify(new Workflows.ChildModifiedEvent(this, j));
	}

	/*
	 * public void setDimensions(HyperJob<V> hj, double[] d) { assert
	 * (children.contains(hj));
	 * 
	 * if (d[0] != hj.dimensions[0] || d[1] != hj.dimensions[1] || d[2] !=
	 * hj.dimensions[2] || d[3] != hj.dimensions[3]) { hj.setDimensions(d);
	 * modifyObservable.notify(new Pair<HyperWorkflow<F, V>, HyperJob<V>>( this,
	 * hj)); } }
	 */

}
