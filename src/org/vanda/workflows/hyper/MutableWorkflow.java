package org.vanda.workflows.hyper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.util.Util;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Jobs.JobEvent;
import org.vanda.workflows.hyper.Jobs.JobListener;
import org.vanda.workflows.hyper.Workflows.*;

public class MutableWorkflow implements JobListener<Job> {

	private final MultiplexObserver<WorkflowEvent<MutableWorkflow>> observable;
	private LinkedList<WorkflowEvent<MutableWorkflow>> events;
	private final Observer<JobEvent<Job>> childObserver;
	private final WeakHashMap<Location, ConnectionKey> varSources;
	private final ArrayList<Job> children;
	private String name;
	private int update = 0;

	{
		childObserver = new Observer<JobEvent<Job>>() {
			@Override
			public void notify(JobEvent<Job> event) {
				event.doNotify(MutableWorkflow.this);
			}
		};
		events = new LinkedList<Workflows.WorkflowEvent<MutableWorkflow>>();
	}

	public MutableWorkflow(String name) {
		this.name = name;
		children = new ArrayList<Job>();
		observable = new MultiplexObserver<WorkflowEvent<MutableWorkflow>>();
		varSources = new WeakHashMap<Location, ConnectionKey>();
	}

	public Collection<Job> getChildren() {
		// better: return Collections.unmodifiableCollection(children);
		return children;
	}

	public Job[] getSorted() throws Exception {
		TopSorter t = new TopSorter();
		t.init(this);
		t.proceed();
		return t.getSorted();
	}

	public void addChild(final Job job) {
		assert (!job.isInserted());
		beginUpdate();
		try {
			job.insert();
			for (Port op : job.getOutputPorts()) {
				Location var = new Location();
				job.bindings.put(op, var);
				varSources.put(var, new ConnectionKey(job, op));
			}
			children.add(job);
			bind(job);
			events.add(new Workflows.ChildAddedEvent<MutableWorkflow>(this, job));
		} finally {
			endUpdate();
		}
	}

	public void addConnection(ConnectionKey cc, Location variable) {
		beginUpdate();
		try {
			Location old = cc.target.bindings.put(cc.targetPort, variable);
			if (old != variable) {
				if (old != null)
					throw new RuntimeException("!!!"); // FIXME better exception
				events.add(new Workflows.ConnectionAddedEvent<MutableWorkflow>(
						this, cc));
			}
		} finally {
			endUpdate();
		}
	}

	public void beginUpdate() {
		update++;
	}

	public void endUpdate() {
		update--;
		if (update == 0) {
			LinkedList<WorkflowEvent<MutableWorkflow>> ev = events;
			events = new LinkedList<Workflows.WorkflowEvent<MutableWorkflow>>();
			Util.notifyAll(observable, ev);
			observable
					.notify(new Workflows.UpdatedEvent<MutableWorkflow>(this));
		}
	}

	public Observable<WorkflowEvent<MutableWorkflow>> getObservable() {
		return observable;
	}

	public Location getConnectionValue(ConnectionKey cc) {
		return cc.target.bindings.get(cc.targetPort);
	}

	public ConnectionKey getConnectionSource(ConnectionKey cc) {
		return getVariableSource(getConnectionValue(cc));
	}

	public ConnectionKey getVariableSource(Location variable) {
		return varSources.get(variable);
	}

	public void removeChild(Job ji) {
		beginUpdate();
		try {
			if (children.remove(ji)) {
				unbind(ji);
				for (Port op : ji.getOutputPorts()) {
					Location var = ji.bindings.get(op);
					for (Job j2 : children)
						for (Port ip : j2.getInputPorts())
							if (j2.bindings.get(ip) == var)
								removeConnection(new ConnectionKey(j2, ip));
					varSources.remove(var);
				}
				for (Port ip : ji.getInputPorts()) {
					removeConnection(new ConnectionKey(ji, ip));
				}
				ji.uninsert();
				events.add(new Workflows.ChildRemovedEvent<MutableWorkflow>(
						this, ji));
			}
		} finally {
			endUpdate();
		}
	}

	public void removeConnection(ConnectionKey cc) {
		beginUpdate();
		try {
			Location old = null;
			if (cc.target.isInserted())
				old = cc.target.bindings.remove(cc.targetPort);
			if (old != null)
				events.add(new Workflows.ConnectionRemovedEvent<MutableWorkflow>(
						this, cc));
		} finally {
			endUpdate();
		}
	}

	public List<ConnectionKey> getConnections() {
		// only for putting existing hypergraphs into the GUI
		LinkedList<ConnectionKey> conn = new LinkedList<ConnectionKey>();
		for (Job ji : children) {
			for (Port ip : ji.getInputPorts())
				if (ji.bindings.containsKey(ip))
					conn.add(new ConnectionKey(ji, ip));
		}
		return conn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (!name.equals(this.name)) {
			this.name = name;
			observable
					.notify(new Workflows.PropertyChangedEvent<MutableWorkflow>(
							this));
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
		for (Job ji : children) {
			ji.rebind();
			bind(ji);
		}
	}

	@Override
	public void propertyChanged(Job j) {
		observable.notify(new Workflows.ChildModifiedEvent<MutableWorkflow>(
				this, j));
	}

	public void typeCheck(TypeChecker tc) throws TypeCheckingException {
		for (Job ji : children) {
			ji.typeCheck();
			ji.addFragmentTypeEquation(tc);
			tc.addDataFlowEquations(ji);
		}
	}

}
