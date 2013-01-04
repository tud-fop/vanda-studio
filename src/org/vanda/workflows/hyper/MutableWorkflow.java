package org.vanda.workflows.hyper;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.hyper.Job.JobEvent;
import org.vanda.workflows.hyper.Job.JobListener;

public final class MutableWorkflow extends DrecksWorkflow implements Cloneable,
		JobListener {

	public static interface WorkflowListener {
		// removed: see older versions
		// void inputPortAdded(MutableWorkflow mwf, int index);
		// void inputPortRemoved(MutableWorkflow mwf, int index);
		// void outputPortAdded(MutableWorkflow mwf, int index);
		// void outputPortRemoved(MutableWorkflow mwf, int index);

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

		// removed: see older versions
		// void inputPortAdded(MutableWorkflow mwf, Job j, int index);
		// void inputPortRemoved(MutableWorkflow mwf, Job j, int index);
		// void outputPortAdded(MutableWorkflow mwf, Job j, int index);
		// void outputPortRemoved(MutableWorkflow mwf, Job j, int index);
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

	public Token addChild(final Job job) {
		assert (job.address == null);
		job.address = childAddressSource.makeToken();
		final DJobInfo ji = new DJobInfo(this, job);
		if (job.address.intValue() < children.size())
			children.set(job.address.intValue(), ji);
		else {
			assert (job.address.intValue() == children.size());
			children.add(ji);
		}
		bind(job);
		// XXX removed: handle ports (see older versions)
		childObservable.notify(new Workflows.ChildAddedEvent(this, job));
		return job.address;
	}

	public Token addConnection(Connection cc) {
		assert (cc.address == null);
		DJobInfo sji = children.get(cc.source.intValue());
		DJobInfo tji = children.get(cc.target.intValue());
		assert (sji.job.getOutputPorts().get(cc.sourcePort) != null);
		assert (tji.job.getInputPorts().get(cc.targetPort) != null);
		if (tji.job.inputs.get(cc.targetPort) != null)
			throw new RuntimeException("!!!"); // FIXME better exception
		Token tok = sji.job.outputs.get(cc.sourcePort);
		DConnInfo ci = new DConnInfo(tok, cc);
		cc.address = connectionAddressSource.makeToken();
		tji.job.inputs.set(cc.targetPort, tok);
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
		if (ji != null && 0 <= sourcePort && sourcePort < ji.job.outputs.size()) {
			return ji.job.outputs.get(sourcePort);
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
		final DJobInfo ji = children.get(address.intValue());
		if (ji == null)
			return;
		// XXX removed: handle ports (see older versions)
		for (int i = 0; i < connections.size(); i++) {
			DConnInfo ci = connections.get(i);
			if (ci != null) {
				if (ci.cc.source == address || ci.cc.target == address)
					removeConnection(ci.cc.address);
			}
		}
		for (int i = 0; i < ji.job.outputs.size(); i++) {
			variableSource.recycleToken(ji.job.outputs.get(i));
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
			tji.job.inputs.set(ci.cc.targetPort, null);
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
