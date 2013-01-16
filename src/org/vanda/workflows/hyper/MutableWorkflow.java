package org.vanda.workflows.hyper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.util.TokenSource;
import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job.JobEvent;
import org.vanda.workflows.hyper.Job.JobListener;
import org.vanda.workflows.immutable.ImmutableWorkflow;
import org.vanda.workflows.immutable.JobInfo;

public final class MutableWorkflow implements Cloneable, JobListener {

	protected static final class DJobInfo {
		public final Job job;
		public int inputsBlocked;
		public int topSortInputsBlocked;
		public Token portNumber = null;

		public DJobInfo(MutableWorkflow parent, Job j) {
			job = j;
			job.inputs.ensureCapacity(j.getInputPorts().size());
			job.inputs.clear();
			for (int i = 0; i < j.getInputPorts().size(); i++)
				job.inputs.add(null);
			inputsBlocked = 0;
			job.outputs.ensureCapacity(j.getOutputPorts().size());
			job.outputs.clear();
			for (int i = 0; i < j.getOutputPorts().size(); i++) {
				Token t = parent.variableSource.makeToken();
				job.outputs.add(t);
			}
			topSortInputsBlocked = 0;
		}

		public DJobInfo(DJobInfo ji) throws CloneNotSupportedException {
			// only apply this when the whole hyperworkflow is copied
			job = ji.job.clone();
			inputsBlocked = ji.inputsBlocked;
			topSortInputsBlocked = ji.topSortInputsBlocked;
		}

	}

	protected final TokenSource variableSource;
	protected final TokenSource childAddressSource;
	// protected final TokenSource connectionAddressSource;
	protected final TokenSource inputPortSource;
	protected final TokenSource outputPortSource;
	protected final ArrayList<DJobInfo> children;
	// protected final Map<Token, Pair<TokenValue<F>, List<TokenValue<F>>>>
	// connections;
	protected final ArrayList<Token> inputPorts;
	protected final ArrayList<Token> outputPorts;
	protected String name;

	public MutableWorkflow(String name) {
		super();
		this.name = name;
		children = new ArrayList<DJobInfo>();
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
		children = new ArrayList<DJobInfo>();
		ListIterator<DJobInfo> it = hyperWorkflow.children.listIterator();
		while (it.hasNext()) {
			DJobInfo ji = it.next();
			if (ji == null)
				children.add(null);
			else
				children.add(new DJobInfo(ji));
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
		for (DJobInfo ji : children)
			if (ji != null)
				result.add(ji.job);
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
				list.add(children.get(t.intValue()).job.getOutputPorts().get(0));
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
				list.add(children.get(t.intValue()).job.getInputPorts().get(0));

		/*
		 * for (DJobInfo ji : children) if (ji != null && ji.job.isOutputPort())
		 * list.add(ji.job.getInputPorts().get(0));
		 */
		return list;
	}

	/*
	 * public Class<F> getFragmentType() { return fragmentType; }
	 */

	public ImmutableWorkflow freeze() throws Exception {
		// Two steps. Step 1: topological sort
		// compute source job for each variable
		Map<Token, DJobInfo> varSource = new HashMap<Token, DJobInfo>();
		for (DJobInfo ji : children) {
			if (ji != null) {
				for (Token t : ji.job.outputs)
					varSource.put(t, ji);
			}
		}
		// compute initial working set (jobs without inputs)
		// also, compute forward array: for each job which job can be reached
		Map<DJobInfo, LinkedList<DJobInfo>> forwA = new HashMap<DJobInfo, LinkedList<DJobInfo>>();
		LinkedList<DJobInfo> workingset = new LinkedList<DJobInfo>();
		int count = 0;
		for (DJobInfo ji : children) {
			if (ji != null) {
				for (Token t : ji.job.inputs) {
					if (t != null) {
						DJobInfo key = varSource.get(t);
						LinkedList<DJobInfo> ll = forwA.get(key);
						if (ll == null) {
							ll = new LinkedList<DJobInfo>();
							forwA.put(key, ll);
						}
						ll.add(ji);
					}
				}
				ji.topSortInputsBlocked = ji.inputsBlocked;
				if (ji.topSortInputsBlocked == 0)
					workingset.add(ji);
				count++;
			}
		}
		// topological sort
		ArrayList<DJobInfo> topsort = new ArrayList<DJobInfo>(count);
		while (!workingset.isEmpty()) {
			DJobInfo ji = workingset.pop();
			topsort.add(ji);
			LinkedList<DJobInfo> ll = forwA.get(ji);
			if (ll != null)
				for (DJobInfo ji2 : ll) {
					ji2.topSortInputsBlocked--;
					if (ji2.topSortInputsBlocked == 0)
						workingset.add(ji2);
				}
		}
		// Step 2: actual freeze
		if (topsort.size() == count) {
			ArrayList<JobInfo> imch = new ArrayList<JobInfo>(topsort.size());
			for (DJobInfo ji : topsort) {
				boolean connected = true;
				List<Port> ports = null;
				ports = ji.job.getInputPorts();
				ArrayList<Token> intoken = new ArrayList<Token>(ports.size());
				for (int i = 0; i < ports.size(); i++) {
					if (ports.get(i) != null) {
						Token t = ji.job.inputs.get(i);
						intoken.add(t);
						connected = connected && (t != null);
					}
				}
				ports = ji.job.getOutputPorts();
				ArrayList<Token> outtoken = new ArrayList<Token>(ports.size());
				for (int i = 0; i < ports.size(); i++) {
					if (ports.get(i) != null)
						outtoken.add(ji.job.outputs.get(i));
				}
				imch.add(new JobInfo(ji.job.freeze(), ji.job.address, intoken,
						outtoken, connected));
			}
			List<Token> ports = null;
			ports = inputPorts;
			ArrayList<Port> inputPorts = new ArrayList<Port>();
			ArrayList<Token> inputPortVariables = new ArrayList<Token>();
			for (int i = 0; i < ports.size(); i++) {
				if (ports.get(i) != null) {
					DJobInfo daPort = children.get(ports.get(i).intValue());
					inputPorts.add(daPort.job.getOutputPorts().get(0));
					inputPortVariables.add(daPort.job.outputs.get(0));
				}
			}
			ports = outputPorts;
			ArrayList<Port> outputPorts = new ArrayList<Port>();
			ArrayList<Token> outputPortVariables = new ArrayList<Token>();
			for (int i = 0; i < ports.size(); i++) {
				if (ports.get(i) != null) {
					DJobInfo daPort = children.get(ports.get(i).intValue());
					outputPorts.add(daPort.job.getInputPorts().get(0));
					outputPortVariables.add(daPort.job.inputs.get(0));
				}
			}
			return new ImmutableWorkflow(name, inputPorts, outputPorts,
					inputPortVariables, outputPortVariables, null, null, imch,
					variableSource, variableSource.getMaxToken());
		} else
			throw new Exception(
					"could not do topological sort; cycles probable");
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

	public void addConnection(ConnectionKey cc, Token variable) {
		DJobInfo tji = children.get(cc.target.intValue());
		assert (tji.job.getInputPorts().get(cc.targetPort) != null);
		Token old = tji.job.inputs.get(cc.targetPort);
		if (old != variable) {
			if (old != null)
				throw new RuntimeException("!!!"); // FIXME better exception
			tji.job.inputs.set(cc.targetPort, variable);
			tji.inputsBlocked++;
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
		return children.get(cc.target.intValue()).job.inputs.get(cc.targetPort);
	}

	public ConnectionKey getConnectionSource(ConnectionKey cc) {
		Token variable = getConnectionValue(cc);
		if (variable != null) {
			for (DJobInfo ji : children) {
				if (ji != null) {
					for (int i = 0; i < ji.job.outputs.size(); i++)
						if (ji.job.outputs.get(i) == variable) {
							return new ConnectionKey(ji.job.address, i);
						}
				}
			}
			return null;
		} else
			return null;
	}

	public void removeChild(Token address) {
		final DJobInfo ji = children.get(address.intValue());
		if (ji == null)
			return;
		// XXX removed: handle ports (see older versions)
		for (int i = 0; i < ji.job.outputs.size(); i++) {
			variableSource.recycleToken(ji.job.outputs.get(i));
		}
		unbind(ji.job);
		children.set(ji.job.address.intValue(), null);
		childObservable.notify(new Workflows.ChildRemovedEvent(this, ji.job));
		ji.job.address = null;
		childAddressSource.recycleToken(address);
	}

	public void removeConnection(ConnectionKey cc) {
		DJobInfo tji = children.get(cc.target.intValue());
		// assert (sji.outputs.get(sourcePort) == tji.inputs.get(ci.port));
		Token old = tji.job.inputs.get(cc.targetPort);
		if (old != null) {
			tji.job.inputs.set(cc.targetPort, null);
			tji.inputsBlocked--;
			childObservable.notify(new Workflows.ConnectionRemovedEvent(this, cc));
		}
	}

	public List<ConnectionKey> getConnections() {
		// only for putting existing hypergraphs into the GUI
		LinkedList<ConnectionKey> conn = new LinkedList<ConnectionKey>();
		for (DJobInfo ji : children) {
			if (ji != null)
				for (int i = 0; i < ji.job.inputs.size(); i++)
					if (ji.job.inputs.get(i) != null)
						conn.add(new ConnectionKey(ji.job.address, i));
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
