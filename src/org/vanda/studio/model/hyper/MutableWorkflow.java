package org.vanda.studio.model.hyper;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource.Token;

public final class MutableWorkflow extends DrecksWorkflow implements Cloneable {

	private final MultiplexObserver<Pair<MutableWorkflow, Job>> addObservable;
	private final MultiplexObserver<Pair<MutableWorkflow, Job>> modifyObservable;
	private final MultiplexObserver<Pair<MutableWorkflow, Job>> removeObservable;
	private final MultiplexObserver<Pair<MutableWorkflow, Connection>> connectObservable;
	private final MultiplexObserver<Pair<MutableWorkflow, Connection>> disconnectObservable;
	private final Observer<Job> nameChangeObserver;
	private final Observer<Job> portsChangeObserver;
	private final MultiplexObserver<MutableWorkflow> nameChangeObservable;

	{
		nameChangeObserver = new Observer<Job>() {
			@Override
			public void notify(Job event) {
				modifyObservable.notify(new Pair<MutableWorkflow, Job>(
						MutableWorkflow.this, event));
			}
		};
		portsChangeObserver = new Observer<Job>() {
			@Override
			public void notify(Job event) {
				modifyObservable.notify(new Pair<MutableWorkflow, Job>(
						MutableWorkflow.this, event));
			}
		};
	}

	public MutableWorkflow(String name) {
		super(name);
		addObservable = new MultiplexObserver<Pair<MutableWorkflow, Job>>();
		modifyObservable = new MultiplexObserver<Pair<MutableWorkflow, Job>>();
		removeObservable = new MultiplexObserver<Pair<MutableWorkflow, Job>>();
		connectObservable = new MultiplexObserver<Pair<MutableWorkflow, Connection>>();
		disconnectObservable = new MultiplexObserver<Pair<MutableWorkflow, Connection>>();
		nameChangeObservable = new MultiplexObserver<MutableWorkflow>();
	}

	public MutableWorkflow(MutableWorkflow hyperWorkflow)
			throws CloneNotSupportedException {
		super(hyperWorkflow);
		addObservable = hyperWorkflow.addObservable.clone();
		modifyObservable = hyperWorkflow.modifyObservable.clone();
		removeObservable = hyperWorkflow.removeObservable.clone();
		connectObservable = hyperWorkflow.connectObservable.clone();
		disconnectObservable = hyperWorkflow.disconnectObservable.clone();
		nameChangeObservable = hyperWorkflow.nameChangeObservable.clone();
	}

	@Override
	public MutableWorkflow clone() throws CloneNotSupportedException {
		return new MutableWorkflow(this);
	}

	public Token addChild(Job job) {
		// TODO do something about ports, and notify!
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
		addObservable.notify(new Pair<MutableWorkflow, Job>(this, job));
		return job.address;
	}

	public Token addConnection(Connection cc) {
		assert (cc.address == null);
		DJobInfo sji = children.get(cc.source.intValue());
		DJobInfo tji = children.get(cc.target.intValue());
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
		// connections.get(tok).snd.add(new TokenValue<F>(cc.getTarget(), cc
		// .getTargetPort()));
		sji.outCount++;
		connectObservable
				.notify(new Pair<MutableWorkflow, Connection>(this, cc));
		return cc.address;
	}

	public Observable<Pair<MutableWorkflow, Job>> getAddObservable() {
		return addObservable;
	}

	public Observable<Pair<MutableWorkflow, Connection>> getConnectObservable() {
		return connectObservable;
	}

	public Observable<Pair<MutableWorkflow, Connection>> getDisconnectObservable() {
		return disconnectObservable;
	}

	public Observable<Pair<MutableWorkflow, Job>> getModifyObservable() {
		return modifyObservable;
	}

	public Observable<MutableWorkflow> getNameChangeObservable() {
		return nameChangeObservable;
	}

	public Observable<Pair<MutableWorkflow, Job>> getRemoveObservable() {
		return removeObservable;
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
		if (ji != null) {
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
			removeObservable
					.notify(new Pair<MutableWorkflow, Job>(this, ji.job));
			ji.job.address = null;
			childAddressSource.recycleToken(address);
		}
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
			disconnectObservable.notify(new Pair<MutableWorkflow, Connection>(
					this, ci.cc));
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
			nameChangeObservable.notify(this);
		}
	}

	private void bind(Job job) {
		register(job.getNameChangeObservable(), nameChangeObserver);
		register(job.getPortsChangeObservable(), portsChangeObserver);
	}

	private void unbind(Job job) {
		unregister(job.getNameChangeObservable(), nameChangeObserver);
		unregister(job.getPortsChangeObservable(), portsChangeObserver);
	}

	private static void register(Observable<Job> obs, Observer<Job> o) {
		if (obs != null)
			obs.addObserver(o);
	}

	private static <F> void unregister(Observable<Job> obs, Observer<Job> o) {
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
