package org.vanda.studio.model.hyper;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource.Token;

public final class MutableWorkflow<F> extends DrecksWorkflow<F> implements
		HyperWorkflow<F>, Cloneable {

	private final MultiplexObserver<Pair<MutableWorkflow<F>, Token>> addObservable;
	private final MultiplexObserver<Pair<MutableWorkflow<F>, Token>> modifyObservable;
	private final MultiplexObserver<Pair<MutableWorkflow<F>, Token>> removeObservable;
	private final MultiplexObserver<Pair<MutableWorkflow<F>, Token>> connectObservable;
	private final MultiplexObserver<Pair<MutableWorkflow<F>, Token>> disconnectObservable;

	public MutableWorkflow(Class<F> fragmentType) {
		super(fragmentType);
		addObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Token>>();
		modifyObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Token>>();
		removeObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Token>>();
		connectObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Token>>();
		disconnectObservable = new MultiplexObserver<Pair<MutableWorkflow<F>, Token>>();
	}

	public MutableWorkflow(MutableWorkflow<F> hyperWorkflow)
			throws CloneNotSupportedException {
		super(hyperWorkflow);
		addObservable = hyperWorkflow.addObservable.clone();
		modifyObservable = hyperWorkflow.modifyObservable.clone();
		removeObservable = hyperWorkflow.removeObservable.clone();
		connectObservable = hyperWorkflow.connectObservable.clone();
		disconnectObservable = hyperWorkflow.disconnectObservable.clone();
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
	public Token addChild(Job<F> hj) {
		assert (hj.getFragmentType() == null || hj.getFragmentType() == getFragmentType());
		DJobInfo<F> ji = new DJobInfo<F>(this, hj);
		if (ji.address.intValue() < children.size())
			children.set(ji.address.intValue(), ji);
		else {
			assert (ji.address.intValue() == children.size());
			children.add(ji);
		}
		/*
		 * if (!children.containsKey(hj)) { children.put(hj, new
		 * DJobInfo<F>(this, hj)); hj.parent = this; addObservable.notify(new
		 * Pair<MutableWorkflow<F>, Job<F>>( this, hj)); }
		 */
		addObservable.notify(new Pair<MutableWorkflow<F>, Token>(this,
				ji.address));
		return ji.address;
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
		DJobInfo<F> sji = children.get(cc.source.intValue());
		DJobInfo<F> tji = children.get(cc.target.intValue());
		if (tji.inputs.get(cc.targetPort) != null)
			throw new RuntimeException("!!!"); // FIXME better exception
		Token tok = sji.outputs.get(cc.sourcePort);
		DConnInfo ci = new DConnInfo(addressSource.makeToken(), tok, cc);
		tji.inputs.set(cc.targetPort, tok);
		tji.inputsBlocked++;
		if (ci.address.intValue() < connections.size())
			connections.set(ci.address.intValue(), ci);
		else {
			assert (ci.address.intValue() == connections.size());
			connections.add(ci);
		}
		// connections.get(tok).snd.add(new TokenValue<F>(cc.getTarget(), cc
		// .getTargetPort()));
		sji.outCount++;
		connectObservable.notify(new Pair<MutableWorkflow<F>, Token>(this,
				ci.address));
		return ci.address;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getAddObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Token>> getAddObservable() {
		return addObservable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getConnectObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Token>> getConnectObservable() {
		return connectObservable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getDisconnectObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Token>> getDisconnectObservable() {
		return disconnectObservable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getModifyObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Token>> getModifyObservable() {
		return modifyObservable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vanda.studio.model.hyper.HyperWorkflow#getRemoveObservable()
	 */
	@Override
	public Observable<Pair<MutableWorkflow<F>, Token>> getRemoveObservable() {
		return removeObservable;
	}

	public Object getVariable(Token source, int sourcePort) {
		DJobInfo<F> ji = children.get(source.intValue());
		if (ji != null && 0 <= sourcePort && sourcePort < ji.outputs.size()) {
			return ji.outputs.get(sourcePort);
		} else
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
						removeConnection(ci.address);
				}
			}
			for (int i = 0; i < ji.outputs.size(); i++) {
				variableSource.recycleToken(ji.outputs.get(i));
			}
			addressSource.recycleToken(ji.address);
			children.set(ji.address.intValue(), null);
			removeObservable.notify(new Pair<MutableWorkflow<F>, Token>(this,
					ji.address));
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
			disconnectObservable.notify(new Pair<MutableWorkflow<F>, Token>(
					this, address));

		}
	}

	@Override
	public Job<?> dereference(ListIterator<Token> address) {
		assert (address != null && address.hasNext());
		DJobInfo<?> ji = children.get(address.next().intValue());
		if (ji != null)
			return ji.job.dereference(address);
		else
			return null;
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
