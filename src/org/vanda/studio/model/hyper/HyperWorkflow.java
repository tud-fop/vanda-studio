package org.vanda.studio.model.hyper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vanda.studio.model.generation.Port;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Pair;

public final class HyperWorkflow<F> {

	private static class TokenValue<F> {
		public final HyperJob<F> hj;
		public final int port;

		public TokenValue(HyperJob<F> hj, int port) {
			this.hj = hj;
			this.port = port;
		}
	}

	private class JobInfo {
		public ArrayList<Integer> inputs;
		public int inputsBlocked;
		public ArrayList<Integer> outputs;
		public int outCount;
		public int topSortInputsBlocked;

		public JobInfo(HyperJob<F> j) {
			inputs = new ArrayList<Integer>(j.getInputPorts().size());
			for (Port p : j.getInputPorts())
				inputs.add(null);
			inputsBlocked = 0;
			outputs = new ArrayList<Integer>(j.getOutputPorts().size());
			for (int i = 0; i < j.getOutputPorts().size(); i++) {
				Integer t = token.makeToken();
				outputs.add(t);
				connections.put(t,
						new Pair<TokenValue<F>, List<TokenValue<F>>>(
								new TokenValue<F>(j, i),
								new LinkedList<TokenValue<F>>()));
			}
			outCount = 0;
			topSortInputsBlocked = 0;
		}

		public JobInfo(JobInfo ji) {
			// only apply this when the whole hyperworkflow is copied
			// only copy inputs, because they are mutable
			inputs = new ArrayList<Integer>(ji.inputs);
			inputsBlocked = ji.inputsBlocked;
			outputs = ji.outputs;
			outCount = ji.outCount;
			topSortInputsBlocked = ji.topSortInputsBlocked;
		}

	}

	private Token token;
	private Map<HyperJob<F>, JobInfo> children;
	private Map<Integer, Pair<TokenValue<F>, List<TokenValue<F>>>> connections;
	private final Class<F> fragmentType;

	private final MultiplexObserver<Pair<HyperWorkflow<F>, HyperJob<F>>> addObservable;
	private final MultiplexObserver<Pair<HyperWorkflow<F>, HyperJob<F>>> modifyObservable;
	private final MultiplexObserver<Pair<HyperWorkflow<F>, HyperJob<F>>> removeObservable;
	private final MultiplexObserver<Pair<HyperWorkflow<F>, HyperConnection<F>>> connectObservable;
	private final MultiplexObserver<Pair<HyperWorkflow<F>, HyperConnection<F>>> disconnectObservable;

	public HyperWorkflow(Class<F> fragmentType) {
		super();
		addObservable = new MultiplexObserver<Pair<HyperWorkflow<F>, HyperJob<F>>>();
		modifyObservable = new MultiplexObserver<Pair<HyperWorkflow<F>, HyperJob<F>>>();
		removeObservable = new MultiplexObserver<Pair<HyperWorkflow<F>, HyperJob<F>>>();
		connectObservable = new MultiplexObserver<Pair<HyperWorkflow<F>, HyperConnection<F>>>();
		disconnectObservable = new MultiplexObserver<Pair<HyperWorkflow<F>, HyperConnection<F>>>();
		children = new HashMap<HyperJob<F>, JobInfo>();
		connections = new HashMap<Integer, Pair<TokenValue<F>, List<TokenValue<F>>>>();
		this.fragmentType = fragmentType;
		token = new Token();
	}

	public HyperWorkflow(HyperWorkflow<F> hyperWorkflow)
			throws CloneNotSupportedException {
		addObservable = hyperWorkflow.addObservable.clone();
		modifyObservable = hyperWorkflow.modifyObservable.clone();
		removeObservable = hyperWorkflow.removeObservable.clone();
		connectObservable = hyperWorkflow.connectObservable.clone();
		disconnectObservable = hyperWorkflow.disconnectObservable.clone();
		children = new HashMap<HyperJob<F>, JobInfo>();
		for (Entry<HyperJob<F>, JobInfo> e : hyperWorkflow.children.entrySet())
			children.put(e.getKey(), new JobInfo(e.getValue()));
		connections = new HashMap<Integer, Pair<TokenValue<F>, List<TokenValue<F>>>>(
				connections);
		fragmentType = hyperWorkflow.fragmentType;
		token = hyperWorkflow.token.clone();
	}

	public HyperWorkflow<F> clone() throws CloneNotSupportedException {
		return new HyperWorkflow<F>(this);
	}

	public void addChild(HyperJob<F> hj) {
		assert (hj.getFragmentType() == null || hj.getFragmentType() == fragmentType);
		assert (hj.parent == null);
		if (!children.containsKey(hj)) {
			children.put(hj, new JobInfo(hj));
			hj.parent = this;
			addObservable.notify(new Pair<HyperWorkflow<F>, HyperJob<F>>(this,
					hj));
		}
	}

	public void addConnection(HyperConnection<F> cc) {
		assert (children.containsKey(cc.getSource()) && children.containsKey(cc
				.getTarget()));
		JobInfo sji = children.get(cc.getSource());
		JobInfo tji = children.get(cc.getTarget());
		if (tji.inputs.get(cc.getTargetPort()) != null)
			throw new RuntimeException("!!!"); // FIXME better exception
		Integer tok = sji.outputs.get(cc.getSourcePort());
		// TODO check for circles
		tji.inputs.set(cc.getTargetPort(), tok);
		tji.inputsBlocked++;
		connections.get(tok).snd.add(new TokenValue<F>(cc.getTarget(), cc
				.getTargetPort()));
		sji.outCount++;
		connectObservable
				.notify(new Pair<HyperWorkflow<F>, HyperConnection<F>>(this, cc));
	}

	public Collection<HyperJob<F>> getChildren() {
		return children.keySet();
	}

	/**
	 * Looks for children that are InputPorts.
	 * 
	 * @return
	 */
	public List<Port> getInputPorts() {
		ArrayList<Port> list = new ArrayList<Port>();
		for (HyperJob<F> c : children.keySet())
			if (c.isInputPort())
				list.add(c.getOutputPorts().get(0));
		return list;
	}

	public Observable<Pair<HyperWorkflow<F>, HyperJob<F>>> getAddObservable() {
		return addObservable;
	}

	public Observable<Pair<HyperWorkflow<F>, HyperConnection<F>>> getConnectObservable() {
		return connectObservable;
	}

	public Observable<Pair<HyperWorkflow<F>, HyperConnection<F>>> getDisconnectObservable() {
		return disconnectObservable;
	}

	public Observable<Pair<HyperWorkflow<F>, HyperJob<F>>> getModifyObservable() {
		return modifyObservable;
	}

	public Observable<Pair<HyperWorkflow<F>, HyperJob<F>>> getRemoveObservable() {
		return removeObservable;
	}

	/**
	 * Looks for children that are OutputPorts.
	 * 
	 * @return
	 */
	public List<Port> getOutputPorts() {
		ArrayList<Port> list = new ArrayList<Port>();
		for (HyperJob<F> c : children.keySet())
			if (c.isOutputPort())
				list.add(c.getInputPorts().get(0));
		return list;
	}

	public static <F> void removeChildGeneric(HyperJob<F> hj) {
		if (hj.parent != null)
			hj.parent.removeChild(hj);
	}

	public void removeChild(HyperJob<F> hj) {
		assert (hj.parent == this);
		JobInfo ji = children.remove(hj);
		if (ji != null) {
			hj.parent = null;
			for (int i = 0; i < ji.inputs.size(); i++) {
				Integer tok = ji.inputs.get(i);
				if (tok != null) {
					TokenValue<F> stv = connections.get(tok).fst;
					children.get(stv.hj).outCount--;
					disconnectObservable
							.notify(new Pair<HyperWorkflow<F>, HyperConnection<F>>(
									this, new HyperConnection<F>(stv.hj,
											stv.port, hj, i)));
				}
			}
			for (int i = 0; i < ji.outputs.size(); i++) {
				for (TokenValue<F> tv : connections.get(ji.outputs.get(i)).snd) {
					JobInfo ji2 = children.get(tv.hj);
					ji2.inputs.set(tv.port, null);
					ji2.inputsBlocked--;
					disconnectObservable
							.notify(new Pair<HyperWorkflow<F>, HyperConnection<F>>(
									this, new HyperConnection<F>(hj, i, tv.hj,
											tv.port)));
				}
				token.recycleToken(i);
			}
			removeObservable.notify(new Pair<HyperWorkflow<F>, HyperJob<F>>(
					this, hj));
		}
	}

	public void removeConnection(HyperConnection<F> cc) {
		if (children.containsKey(cc.getSource())
				&& children.containsKey(cc.getTarget())) {
			JobInfo sji = children.get(cc.getSource());
			JobInfo tji = children.get(cc.getTarget());
			assert (sji.outputs.get(cc.getSourcePort()) == tji.inputs.get(cc
					.getTargetPort()));
			tji.inputs.set(cc.getTargetPort(), null);
			tji.inputsBlocked--;
			ListIterator<TokenValue<F>> li = connections.get(sji.outputs.get(cc
					.getSourcePort())).snd.listIterator();
			while (li.hasNext()) {
				TokenValue<F> tv = li.next();
				if (tv.hj == cc.getSource() && tv.port == cc.getSourcePort())
					li.remove();
			}
			sji.outCount--;
			disconnectObservable
					.notify(new Pair<HyperWorkflow<F>, HyperConnection<F>>(
							this, cc));
		}
	}

	/*
	 * public void removeConnection(HyperJob<F> source, int sourcePort,
	 * HyperJob<F> target, int targetPort) { assert
	 * (children.containsKey(source) && children.containsKey(target)); JobInfo
	 * sji = children.get(source); JobInfo tji = children.get(target); assert
	 * (sji.outputs.get(sourcePort) == tji.inputs.get(targetPort));
	 * tji.inputs.set(targetPort, null); tji.inputsBlocked--;
	 * ListIterator<TokenValue<F>> li = connections.get(sji.outputs
	 * .get(sourcePort)).snd.listIterator(); while (li.hasNext()) {
	 * TokenValue<F> tv = li.next(); if (tv.hj == source && tv.port ==
	 * sourcePort) li.remove(); } sji.outCount--; disconnectObservable
	 * .notify(new Pair<HyperWorkflow<F>, HyperConnection<F>>(this, new
	 * HyperConnection<F>(source, sourcePort, target, targetPort))); }
	 */

	/*
	 * public void setDimensions(HyperJob<V> hj, double[] d) { assert
	 * (children.contains(hj));
	 * 
	 * if (d[0] != hj.dimensions[0] || d[1] != hj.dimensions[1] || d[2] !=
	 * hj.dimensions[2] || d[3] != hj.dimensions[3]) { hj.setDimensions(d);
	 * modifyObservable.notify(new Pair<HyperWorkflow<F, V>, HyperJob<V>>( this,
	 * hj)); } }
	 */

	/**
	 * An intermediate representation in which some Choice nodes may have been
	 * resolved.
	 * 
	 * @author mbue
	 * 
	 */
	private class PartiallyUnfolded {
		private final Set<HyperJob<F>> remaining;
		private final LinkedList<Choice<F>> remainingOr;
		private final Map<HyperJob<F>, JobInfo> extraMap;

		public PartiallyUnfolded() {
			/*
			 * include Choice nodes in the set of remaining nodes for later
			 * conversion
			 */
			remaining = new HashSet<HyperJob<F>>(children.keySet());
			remainingOr = new LinkedList<Choice<F>>();
			for (HyperJob<F> c : children.keySet()) {
				if (c instanceof Choice<?>)
					remainingOr.add((Choice<F>) c);
			}
			extraMap = new HashMap<HyperJob<F>, JobInfo>();
		}

		private PartiallyUnfolded(PartiallyUnfolded parent) {
			remaining = new HashSet<HyperJob<F>>(parent.remaining);
			remainingOr = new LinkedList<Choice<F>>(parent.remainingOr);
			extraMap = new HashMap<HyperJob<F>, JobInfo>(parent.extraMap);
		}

		public boolean isFinal() {
			return remainingOr.isEmpty();
		}

		public void expand(LinkedList<PartiallyUnfolded> pipeline) {
			Choice<F> c = remainingOr.poll();
			if (c != null) {
				JobInfo ji = children.get(c);
				for (int i = 0; i < ji.inputs.size(); i++) {
					if (ji.inputs.get(i) != null) {
						PartiallyUnfolded newone = new PartiallyUnfolded(this);
						newone.cropOr(c, i);
						pipeline.add(newone);
					}
				}
			}
		}

		/**
		 * Removes all ingoing edges to a Choice node except the given one (the
		 * node is determined by the edge).
		 * 
		 * @param cc
		 */
		private void cropOr(Choice<F> c, int i) {
			// copy the JobInfo se we can set all but one inputs to null
			// move the relevant input to the first position for later
			// conversion
			JobInfo ji = new JobInfo(children.get(c));
			Integer tok = ji.inputs.get(i);
			for (int j = 0; j < ji.inputs.size(); j++) {
				if (j != i && ji.inputs.get(j) != null) {
					removeConnection(ji.inputs.get(j));
				}
				ji.inputs.set(j, null);
			}
			ji.inputs.set(0, tok);
		}

		/**
		 * Removes the given connection, then checks whether its source no
		 * longer has outgoing connections, in which case that is removed as
		 * well, and the removal continues with its ingoing connections.
		 * 
		 * @param cc
		 */
		private void removeConnection(Integer i) {
			HyperJob<F> s = connections.get(i).fst.hj;
			JobInfo ji = extraMap.get(s);
			if (ji == null) {
				ji = children.get(s);
				if (ji.outCount > 1 || s.isInputPort())
					extraMap.put(s, ji);
			}
			ji.outCount--;
			if (ji.outCount == 0 && !s.isInputPort()) {
				remaining.remove(s);
				for (int j = 0; j < ji.inputs.size(); j++)
					removeConnection(ji.inputs.get(j));
			}
		}
	}

	/**
	 * This class facilitates the enumeration of all possible combinations of
	 * children. It keeps a list of jobs and an iterator. When a new combination
	 * is required, you call advance. If the list of jobs is exhausted, advance
	 * returns true, which means you have to carry on with the next Counter.
	 * 
	 * @author mbue
	 * 
	 * @param <V>
	 */
	private static class NAryJobCounter<F> {
		private final List<HyperJob<F>> jobs;
		private ListIterator<HyperJob<F>> iterator;
		private HyperJob<F> current;

		public NAryJobCounter(List<HyperJob<F>> jobs) {
			assert (!jobs.isEmpty());
			this.jobs = jobs;
			iterator = jobs.listIterator();
			current = iterator.next();
		}

		/**
		 * Advances to the next job. Returns true if carry is needed.
		 * 
		 * @return
		 */
		public boolean advance() {
			boolean carry = !iterator.hasNext();
			if (carry)
				iterator = jobs.listIterator();
			current = iterator.next();
			return carry;
		}

		public HyperJob<F> getCurrent() {
			return current;
		}

		public boolean isReset() {
			return !iterator.hasPrevious();
		}
	}

	public List<HyperWorkflow<F>> unfold() throws CloneNotSupportedException {
		LinkedList<HyperWorkflow<F>> result = new LinkedList<HyperWorkflow<F>>();
		/*
		 * step 1: unfold children separately, putting everything into a map
		 */
		Map<HyperJob<F>, NAryJobCounter<F>> damap = new HashMap<HyperJob<F>, NAryJobCounter<F>>();
		for (HyperJob<F> c : children.keySet()) {
			List<HyperJob<F>> js = c.unfold();
			if (js != null)
				damap.put(c, new NAryJobCounter<F>(js));
		}
		/*
		 * step 2: resolve Choice nodes, pruning everything that is no longer
		 * connected
		 */
		LinkedList<PartiallyUnfolded> workinglist = new LinkedList<PartiallyUnfolded>();
		LinkedList<PartiallyUnfolded> fynal = new LinkedList<PartiallyUnfolded>();
		workinglist.add(new PartiallyUnfolded());
		while (!workinglist.isEmpty()) {
			PartiallyUnfolded p = workinglist.pop();
			if (p.isFinal())
				fynal.add(p);
			else
				p.expand(workinglist);
		}
		/*
		 * step 3: substitute all combinations from step 1 into the results of
		 * step 2
		 */
		NAryJobCounter<?>[] current = new NAryJobCounter<?>[children.size()];
		for (PartiallyUnfolded p : fynal) {
			/*
			 * Assemble the array of Counters for the remaining hyperjobs in p.
			 * It is crucial that we only consider the remaining hyperjobs,
			 * otherwise we compute duplicate JobWorkflows
			 */
			Iterator<HyperJob<F>> it = p.remaining.iterator();
			int i = 0;
			while (it.hasNext()) {
				HyperJob<F> hj = it.next();
				NAryJobCounter<F> c = damap.get(hj);
				if (c != null) {
					current[i] = c;
					assert (c.isReset());
					i++;
				}
			}
			int clength = i;
			/*
			 * go through all combinations and create the corresponding
			 * JobWorkflows
			 */
			boolean carry = clength == 0;
			while (!carry) {
				HyperWorkflow<F> wf = new HyperWorkflow<F>(this);
				// map remaining children
				for (HyperJob<F> j : p.remaining)
					wf.addChild(damap.get(j).getCurrent());
				/*
				 * // map remaining connections for (Connection<HyperJob<V>> cc
				 * : connections) { if (p.remaining.contains(cc.getSource()) &&
				 * p.remaining.contains(cc.getTarget())) { JobConnection<V> ccn;
				 * // input port change for Choice nodes if (cc.getTarget()
				 * instanceof Choice<?>) { ccn = new JobConnection<V>(damap
				 * .get(cc.getSource()).getCurrent(), cc.getSourcePort(),
				 * damap.get( cc.getTarget()).getCurrent(), 0, cc); } else { ccn
				 * = new JobConnection<V>(damap
				 * .get(cc.getSource()).getCurrent(), cc.getSourcePort(),
				 * damap.get( cc.getTarget()).getCurrent(), cc.getTargetPort(),
				 * cc); } wf.addConnection(ccn); } }
				 */
				result.add(wf);
				// find next combination
				carry = true;
				i = 0;
				while (carry && i < clength)
					carry = current[i].advance();
			}
			// since carry is true at this point, all Helpers should be reset
		}
		return result;
	}

}
