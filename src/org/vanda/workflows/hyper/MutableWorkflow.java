package org.vanda.workflows.hyper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.util.TokenSource;
import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job.JobEvent;
import org.vanda.workflows.hyper.Job.JobListener;
import org.vanda.workflows.immutable.ImmutableWorkflow;

public final class MutableWorkflow implements Cloneable, JobListener {
	
	protected final TokenSource variableSource;
	protected final TokenSource childAddressSource;
	// protected final TokenSource connectionAddressSource;
	protected final TokenSource inputPortSource;
	protected final TokenSource outputPortSource;
	protected final ArrayList<Job> children;
	// protected final Map<Token, Pair<TokenValue<F>, List<TokenValue<F>>>>
	// connections;
	protected final ArrayList<Token> inputPorts;
	protected final ArrayList<Token> outputPorts;
	protected String name;

	public MutableWorkflow(String name) {
		super();
		this.name = name;
		children = new ArrayList<Job>();
		variableSource = new TokenSource();
		childAddressSource = new TokenSource();
		// connectionAddressSource = new TokenSource();
		inputPortSource = new TokenSource();
		outputPortSource = new TokenSource();
		inputPorts = new ArrayList<Token>();
		outputPorts = new ArrayList<Token>();
		observable = new MultiplexObserver<MutableWorkflow.WorkflowEvent>();
		childObservable = new MultiplexObserver<MutableWorkflow.WorkflowChildEvent>();
	}

	public MutableWorkflow(MutableWorkflow hyperWorkflow)
			throws CloneNotSupportedException {
		name = hyperWorkflow.name;
		// clone children because they may contain mutable elements
		children = new ArrayList<Job>();
		ListIterator<Job> it = hyperWorkflow.children.listIterator();
		while (it.hasNext()) {
			Job ji = it.next();
			if (ji == null)
				children.add(null);
			else
				children.add(ji.clone());
		}
		variableSource = hyperWorkflow.variableSource.clone();
		childAddressSource = hyperWorkflow.childAddressSource.clone();
		// connectionAddressSource =
		// hyperWorkflow.connectionAddressSource.clone();
		inputPortSource = hyperWorkflow.inputPortSource.clone();
		outputPortSource = hyperWorkflow.outputPortSource.clone();
		inputPorts = new ArrayList<Token>(hyperWorkflow.inputPorts);
		outputPorts = new ArrayList<Token>(hyperWorkflow.outputPorts);
		observable = hyperWorkflow.observable.clone();
		childObservable = hyperWorkflow.childObservable.clone();
	}

	public Collection<Job> getChildren() {
		ArrayList<Job> result = new ArrayList<Job>();
		for (Job ji : children)
			if (ji != null)
				result.add(ji);
		return result;
	}

	/**
	 * Looks for children that are InputPorts.
	 * 
	 * @return
	 */
	public List<Port> getInputPorts() {
		ArrayList<Port> list = new ArrayList<Port>();
		for (Token t : inputPorts)
			if (t == null)
				list.add(null);
			else
				list.add(children.get(t.intValue()).getOutputPorts().get(0));
		/*
		 * for (DJobInfo ji : children) if (ji != null && ji.job.isInputPort())
		 * list.add(ji.job.getOutputPorts().get(0));
		 */
		return list;
	}

	/**
	 * Looks for children that are OutputPorts.
	 * 
	 * @return
	 */
	public List<Port> getOutputPorts() {
		ArrayList<Port> list = new ArrayList<Port>();
		for (Token t : outputPorts)
			if (t == null)
				list.add(null);
			else
				list.add(children.get(t.intValue()).getInputPorts().get(0));
		return list;
	}

	public ImmutableWorkflow freeze() throws Exception {
		return new Freezer().freeze(this);
	}

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

		void connectionAdded(MutableWorkflow mwf, ConnectionKey cc);

		void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc);

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

	@Override
	public MutableWorkflow clone() throws CloneNotSupportedException {
		return new MutableWorkflow(this);
	}

	public Token addChild(final Job job) {
		assert (job.address == null);
		job.insert(childAddressSource.makeToken());
		for (int i = 0; i < job.getOutputPorts().size(); i++) {
			Token t = variableSource.makeToken();
			job.outputs[i] = t;
		}
		if (job.address.intValue() < children.size())
			children.set(job.address.intValue(), job);
		else {
			assert (job.address.intValue() == children.size());
			children.add(job);
		}
		bind(job);
		// XXX removed: handle ports (see older versions)
		childObservable.notify(new Workflows.ChildAddedEvent(this, job));
		return job.address;
	}

	public void addConnection(ConnectionKey cc, Token variable) {
		Job tji = children.get(cc.target.intValue());
		assert (tji.getInputPorts().get(cc.targetPort) != null);
		Token old = tji.inputs[cc.targetPort];
		if (old != variable) {
			if (old != null)
				throw new RuntimeException("!!!"); // FIXME better exception
			tji.inputs[cc.targetPort] = variable;
			// tji.inputsBlocked++;
			childObservable.notify(new Workflows.ConnectionAddedEvent(this, cc));
		}
	}

	public Observable<WorkflowEvent> getObservable() {
		return observable;
	}

	public Observable<WorkflowChildEvent> getChildObservable() {
		return childObservable;
	}

	public Token getConnectionValue(ConnectionKey cc) {
		return children.get(cc.target.intValue()).inputs[cc.targetPort];
	}

	public ConnectionKey getConnectionSource(ConnectionKey cc) {
		Token variable = getConnectionValue(cc);
		if (variable != null) {
			for (Job ji : children) {
				if (ji != null) {
					for (int i = 0; i < ji.outputs.length; i++)
						if (ji.outputs[i] == variable) {
							return new ConnectionKey(ji.address, i);
						}
				}
			}
			return null;
		} else
			return null;
	}

	public Token getVariable(Job job, int port) {
		return job.outputs[port];
	}

	public void removeChild(Token address) {
		final Job ji = children.get(address.intValue());
		if (ji == null)
			return;
		// XXX removed: handle ports (see older versions)
		for (int i = 0; i < ji.outputs.length; i++) {
			variableSource.recycleToken(ji.outputs[i]);
		}
		unbind(ji);
		children.set(ji.address.intValue(), null);
		childObservable.notify(new Workflows.ChildRemovedEvent(this, ji));
		ji.address = null;
		childAddressSource.recycleToken(address);
	}

	public void removeConnection(ConnectionKey cc) {
		Job tji = children.get(cc.target.intValue());
		// assert (sji.outputs.get(sourcePort) == tji.inputs.get(ci.port));
		Token old = tji.inputs[cc.targetPort];
		if (old != null) {
			tji.inputs[cc.targetPort] = null;
			// tji.inputsBlocked--;
			childObservable.notify(new Workflows.ConnectionRemovedEvent(this, cc));
		}
	}

	public List<ConnectionKey> getConnections() {
		// only for putting existing hypergraphs into the GUI
		LinkedList<ConnectionKey> conn = new LinkedList<ConnectionKey>();
		for (Job ji : children) {
			if (ji != null)
				for (int i = 0; i < ji.inputs.length; i++)
					if (ji.inputs[i] != null)
						conn.add(new ConnectionKey(ji.address, i));
		}
		return conn;
	}

	public Job getChild(Token address) {
		return children.get(address.intValue());
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
		for (Job ji : children)
			if (ji != null) {
				ji.rebind();
				bind(ji);
			}
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
