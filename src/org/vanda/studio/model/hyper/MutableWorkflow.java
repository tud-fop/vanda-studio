package org.vanda.studio.model.hyper;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource.Token;

public final class MutableWorkflow<F> extends DrecksWorkflow<F> implements
		HyperWorkflow<F>, Cloneable {

	private String name;
	private final MultiplexObserver<Pair<MutableWorkflow<F>, Job<F>>> addObservable;
	private final MultiplexObserver<Pair<MutableWorkflow<F>, Job<F>>> modifyObservable;
	private final MultiplexObserver<Pair<MutableWorkflow<F>, Job<F>>> removeObservable;
	private final MultiplexObserver<Pair<MutableWorkflow<F>, Connection>> connectObservable;
	private final MultiplexObserver<Pair<MutableWorkflow<F>, Connection>> disconnectObservable;
	private final Observer<Job<F>> nameChangeObserver;
	private final Observer<Job<F>> portsChangeObserver;
	private final MultiplexObserver<MutableWorkflow<F>> nameChangeObservable;
	
	{
		nameChangeObserver = new Observer<Job<F>>() {
			@Override
			public void notify(Job<F> event) {
				modifyObservable.notify(new Pair<MutableWorkflow<F>, Job<F>>(MutableWorkflow.this, event));
			}
		};
		portsChangeObserver = new Observer<Job<F>>() {
			@Override
			public void notify(Job<F> event) {
				modifyObservable.notify(new Pair<MutableWorkflow<F>, Job<F>>(MutableWorkflow.this, event));
			}
		};
	}

	public MutableWorkflow(Class<F> fragmentType) {
		super(fragmentType);
		addObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Job<F>>>();
		modifyObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Job<F>>>();
		removeObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Job<F>>>();
		connectObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Connection>>();
		disconnectObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Connection>>();
		nameChangeObservable = new MultiplexObserver<MutableWorkflow<F>>();
	}

	public MutableWorkflow(MutableWorkflow<F> hyperWorkflow)
			throws CloneNotSupportedException {
		super(hyperWorkflow);
		name = hyperWorkflow.name;
		addObservable = hyperWorkflow.addObservable.clone();
		modifyObservable = hyperWorkflow.modifyObservable.clone();
		removeObservable = hyperWorkflow.removeObservable.clone();
		connectObservable = hyperWorkflow.connectObservable.clone();
		disconnectObservable = hyperWorkflow.disconnectObservable.clone();
		nameChangeObservable = hyperWorkflow.nameChangeObservable.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#clone()
	 */
	@Override
	public MutableWorkflow<F> clone() throws CloneNotSupportedException {
		return new MutableWorkflow<F>(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.vanda.studio.model.hyper.HyperWorkflow#addChild(org.vanda.studio.
	 * model.hyper.HyperJob)
	 */
	@Override
	public Token addChild(Job<F> job) {
		assert (job.address == null && (job.getFragmentType() == null || job
				.getFragmentType() == getFragmentType()));
		job.address = childAddressSource.makeToken();
		DJobInfo<F> ji = new DJobInfo<F>(this, job);
		if (job.address.intValue() < children.size())
			children.set(job.address.intValue(), ji);
		else {
			assert (job.address.intValue() == children.size());
			children.add(ji);
		}
		bind(job);
		addObservable.notify(new Pair<MutableWorkflow<F>, Job<F>>(this, job));
		return job.address;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.vanda.studio.model.hyper.HyperWorkflow#addConnection(org.vanda.studio
	 * .model.hyper.Connection)
	 */
	@Override
	public Token addConnection(Connection cc) {
		assert (cc.address == null);
		DJobInfo<F> sji = children.get(cc.source.intValue());
		DJobInfo<F> tji = children.get(cc.target.intValue());
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
		connectObservable.notify(new Pair<MutableWorkflow<F>, Connection>(this,
				cc));
		return cc.address;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getAddObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Job<F>>> getAddObservable() {
		return addObservable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getConnectObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Connection>> getConnectObservable() {
		return connectObservable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getDisconnectObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Connection>> getDisconnectObservable() {
		return disconnectObservable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getModifyObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Job<F>>> getModifyObservable() {
		return modifyObservable;
	}

	public Observable<MutableWorkflow<F>> getNameChangeObservable() {
		return nameChangeObservable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getRemoveObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Job<F>>> getRemoveObservable() {
		return removeObservable;
	}

	@Override
	public Token getVariable(Token source, int sourcePort) {
		DJobInfo<F> ji = children.get(source.intValue());
		if (ji != null && 0 <= sourcePort && sourcePort < ji.outputs.size()) {
			return ji.outputs.get(sourcePort);
		} else
			return null;
	}

	@Override
	public Token getVariable(Token address) {
		DConnInfo ci = connections.get(address.intValue());
		if (ci != null)
			return ci.variable;
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.vanda.studio.model.hyper.HyperWorkflow#removeChild(org.vanda.studio
	 * .model.hyper.HyperJob)
	 */
	@Override
	public void removeChild(Token address) {
		DJobInfo<F> ji = children.get(address.intValue());
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
			removeObservable.notify(new Pair<MutableWorkflow<F>, Job<F>>(this,
					ji.job));
			ji.job.address = null;
			childAddressSource.recycleToken(address);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.vanda.studio.model.hyper.HyperWorkflow#removeConnection(org.vanda
	 * .studio.model.hyper.Connection)
	 */
	@Override
	public void removeConnection(Token address) {
		DConnInfo ci = connections.get(address.intValue());
		if (ci != null) {
			DJobInfo<F> sji = children.get(ci.cc.source.intValue());
			DJobInfo<F> tji = children.get(ci.cc.target.intValue());
			// assert (sji.outputs.get(sourcePort) == tji.inputs.get(ci.port));
			tji.inputs.set(ci.cc.targetPort, null);
			tji.inputsBlocked--;
			sji.outCount--;
			connections.set(address.intValue(), null);
			disconnectObservable
					.notify(new Pair<MutableWorkflow<F>, Connection>(this,
							ci.cc));
			ci.cc.address = null;
			connectionAddressSource.recycleToken(address);
		}
	}

	@Override
	public HyperWorkflow<?> dereference(ListIterator<Token> address) {
		assert (address != null);
		if (address.hasNext()) {
			DJobInfo<?> ji = children.get(address.next().intValue());
			if (ji != null)
				return ji.job.dereference(address);
			else
				return null;
		} else
			return this;
	}

	@Override
	public List<Connection> getConnections() {
		// only for putting existing HyperGraphs into the GUI
		LinkedList<Connection> conn = new LinkedList<Connection>();
		for (DConnInfo ci : connections) {
			if (ci != null)
				conn.add(ci.cc);
		}
		return conn;
	}

	@Override
	public Job<F> getChild(Token address) {
		DJobInfo<F> ji = children.get(address.intValue());
		if (ji != null)
			return ji.job;
		else
			return null;
	}

	@Override
	public Connection getConnection(Token address) {
		DConnInfo ci = connections.get(address.intValue());
		if (ci != null)
			return ci.cc;
		else
			return null;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (!name.equals(this.name)) {
			this.name = name;
			nameChangeObservable.notify(this);
		}
	}
	
	private void bind(Job<F> job) {
		register(job.getNameChangeObservable(), nameChangeObserver);
		register(job.getPortsChangeObservable(), portsChangeObserver);
	}
	
	private void unbind(Job<F> job) {
		unregister(job.getNameChangeObservable(), nameChangeObserver);
		unregister(job.getPortsChangeObservable(), portsChangeObserver);
	}
	
	private static <F> void register(Observable<Job<F>> obs, Observer<Job<F>> o) {
		if (obs != null)
			obs.addObserver(o);
	}
	
	private static <F> void unregister(Observable<Job<F>> obs, Observer<Job<F>> o) {
		if (obs != null)
			obs.removeObserver(o);
	}
	
	/**
	 * Call this after deserialization.
	 */
	public void rebind() {
		for (DJobInfo<F> ji : children)
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
