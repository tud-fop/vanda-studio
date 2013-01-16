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
import org.vanda.workflows.immutable.JobInfo;

public final class MutableWorkflow implements Cloneable, JobListener {

	protected static final class DConnInfo {
		public final Token variable;
		public final Connection cc; // somewhat redundant

		public DConnInfo(Token variable, Connection cc) {
			this.variable = variable;
			this.cc = cc;
		}
	}

	protected static final class DJobInfo {
		public final Job job;
		public int inputsBlocked;
		public int outCount;
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
			outCount = 0;
			topSortInputsBlocked = 0;
		}

		public DJobInfo(DJobInfo ji) throws CloneNotSupportedException {
			// only apply this when the whole hyperworkflow is copied
			job = ji.job.clone();
			inputsBlocked = ji.inputsBlocked;
			outCount = ji.outCount;
			topSortInputsBlocked = ji.topSortInputsBlocked;
		}

	}

	protected final TokenSource variableSource;
	protected final TokenSource childAddressSource;
	protected final TokenSource connectionAddressSource;
	protected final TokenSource inputPortSource;
	protected final TokenSource outputPortSource;
	protected final ArrayList<DJobInfo> children;
	// protected final Map<Token, Pair<TokenValue<F>, List<TokenValue<F>>>>
	// connections;
	protected final ArrayList<DConnInfo> connections;
	protected final ArrayList<Token> inputPorts;
	protected final ArrayList<Token> outputPorts;
	protected String name;

	public MutableWorkflow(String name) {
		super();
		this.name = name;
		children = new ArrayList<DJobInfo>();
		connections = new ArrayList<DConnInfo>();
		variableSource = new TokenSource();
		childAddressSource = new TokenSource();
		connectionAddressSource = new TokenSource();
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
		connections = new ArrayList<DConnInfo>(hyperWorkflow.connections);
		variableSource = hyperWorkflow.variableSource.clone();
		childAddressSource = hyperWorkflow.childAddressSource.clone();
		connectionAddressSource = hyperWorkflow.connectionAddressSource.clone();
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
		// XXX potential optimization: compute forward star
		int count = 0;
		LinkedList<DJobInfo> workingset = new LinkedList<DJobInfo>();
		for (DJobInfo ji : children) {
			if (ji != null) {
				ji.topSortInputsBlocked = ji.inputsBlocked;
				if (ji.topSortInputsBlocked == 0)
					workingset.add(ji);
				count++;
			}
		}
		ArrayList<DJobInfo> topsort = new ArrayList<DJobInfo>(count);
		while (!workingset.isEmpty()) {
			DJobInfo ji = workingset.pop();
			topsort.add(ji);
			for (Token tok : ji.job.outputs)
				for (DConnInfo ci : connections) {
					if (ci != null && ci.variable == tok) {
						DJobInfo ji2 = children.get(ci.cc.target.intValue());
						ji2.topSortInputsBlocked--;
						if (ji2.topSortInputsBlocked == 0)
							workingset.add(ji2);
					}
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
						outtoken, ji.outCount, connected));
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

	/*
	 * public MutableWorkflow dereference(ListIterator<Token> address) { assert
	 * (address != null); if (address.hasNext()) { DJobInfo ji =
	 * children.get(address.next().intValue()); if (ji != null) return
	 * ji.job.dereference(address); else return null; } else return this; }
	 */

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
