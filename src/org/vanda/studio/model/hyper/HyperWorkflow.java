package org.vanda.studio.model.hyper;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.vanda.studio.model.generation.Connection;
import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.generation.Workflow;
import org.vanda.studio.model.workflows.Compiler;
import org.vanda.studio.model.workflows.Job;
import org.vanda.studio.model.workflows.JobConnection;
import org.vanda.studio.model.workflows.JobWorkflow;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Pair;

public final class HyperWorkflow<F, V> extends
		Workflow<HyperJob<V>, HyperConnection<V>> implements Cloneable {

	private Map<HyperJob<V>, BitSet> blockedPortsMap;
	private final Compiler<F, V> compiler;
	// not final because of clone():
	private MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperJob<V>>> addObservable;
	private MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperJob<V>>> modifyObservable;
	private MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperJob<V>>> removeObservable;
	private MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperConnection<V>>> connectObservable;
	private MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperConnection<V>>> disconnectObservable;
	
	public HyperWorkflow(Compiler<F, V> compiler) {
		super();
		this.compiler = compiler;
		blockedPortsMap = null; // will be computed on demand
		addObservable = new MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperJob<V>>>();
		modifyObservable = new MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperJob<V>>>();
		removeObservable = new MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperJob<V>>>();
		connectObservable = new MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperConnection<V>>>();
		disconnectObservable = new MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperConnection<V>>>();
	}

	public HyperWorkflow<F, V> clone() throws CloneNotSupportedException {
		@SuppressWarnings("unchecked")
		HyperWorkflow<F, V> cl = (HyperWorkflow<F, V>) super.clone();
		cl.blockedPortsMap = null; // will be computed on demand
		cl.addObservable = addObservable.clone();
		cl.modifyObservable = modifyObservable.clone();
		cl.removeObservable = removeObservable.clone();
		cl.connectObservable = connectObservable.clone();
		cl.disconnectObservable = disconnectObservable.clone();

		cl.children = new HashSet<HyperJob<V>>();
		// NOTE Connection objects are deemed immutable
		cl.connections = new HashSet<HyperConnection<V>>(connections);
		// NOTE HyperJob objects are mutable (especially CompositeHyperJobs)
		for (HyperJob<V> c : children) {
			HyperJob<V> ccl = c.clone();
			ccl.parent = cl;
			cl.children.add(ccl);
		}
		return cl;
	}

	@Override
	public void addChild(HyperJob<V> hj) {
		assert (hj.getViewType() == null || hj.getViewType() == compiler.getViewType());
		assert (hj.parent == null);
		if (children.add(hj)) {
			hj.parent = this;
			addObservable.notify(new Pair<HyperWorkflow<F, V>, HyperJob<V>>(
					this, hj));
		}
	}

	@Override
	public void addConnection(HyperConnection<V> cc) {
		assert (cc.parent == null);
		assert (children.contains(cc.getSource()) && children.contains(cc
				.getTarget()));
		if (!connections.contains(cc)) {
			if (isPortBlocked(cc.getTarget(), cc.getTargetPort()))
				throw new RuntimeException("!!!"); // FIXME better exception
			// TODO check for circles
			connections.add(cc);
			cc.parent = this;
			setPortBlocked(cc.getTarget(), cc.getTargetPort(), true);
			connectObservable
					.notify(new Pair<HyperWorkflow<F, V>, HyperConnection<V>>(
							this, cc));
		}

	}

	protected Map<HyperJob<V>, BitSet> computeBlockedPortsMap() {
		Map<HyperJob<V>, BitSet> result = new HashMap<HyperJob<V>, BitSet>();
		for (Connection<HyperJob<V>> cc : connections) {
			HyperJob<V> t = cc.getTarget();
			int tp = cc.getTargetPort();
			BitSet blocked = result.get(t);
			if (blocked == null) {
				blocked = new BitSet();
				result.put(t, blocked);
			}
			assert (!blocked.get(tp));
			blocked.set(tp);
		}
		return result;
	}

	protected boolean isPortBlocked(HyperJob<V> hj, int index) {
		if (blockedPortsMap == null)
			blockedPortsMap = computeBlockedPortsMap();
		BitSet blocked = blockedPortsMap.get(hj);
		if (blocked == null) {
			return false;
		} else
			return blocked.get(index);
	}

	public Collection<HyperJob<V>> getChildren() {
		return children;
	}

	public Collection<HyperConnection<V>> getConnections() {
		return connections;
	}

	/**
	 * Looks for children that are InputPorts.
	 * 
	 * @return
	 */
	public List<Port> getInputPorts() {
		ArrayList<Port> list = new ArrayList<Port>();
		for (HyperJob<V> c : children)
			if (c.isInputPort())
				list.add(c.getOutputPorts().get(0));
		return list;
	}

	public Observable<Pair<HyperWorkflow<F, V>, HyperJob<V>>> getAddObservable() {
		return addObservable;
	}

	public Observable<Pair<HyperWorkflow<F, V>, HyperConnection<V>>> getConnectObservable() {
		return connectObservable;
	}

	public Observable<Pair<HyperWorkflow<F, V>, HyperConnection<V>>> getDisconnectObservable() {
		return disconnectObservable;
	}

	public Observable<Pair<HyperWorkflow<F, V>, HyperJob<V>>> getModifyObservable() {
		return modifyObservable;
	}

	public Observable<Pair<HyperWorkflow<F, V>, HyperJob<V>>> getRemoveObservable() {
		return removeObservable;
	}

	/**
	 * Looks for children that are OutputPorts.
	 * 
	 * @return
	 */
	public List<Port> getOutputPorts() {
		ArrayList<Port> list = new ArrayList<Port>();
		for (HyperJob<V> c : children)
			if (c.isOutputPort())
				list.add(c.getInputPorts().get(0));
		return list;
	}

	public static <V> void removeChildGeneric(HyperJob<V> hj) {
		if (hj.parent != null)
			hj.parent.removeChild(hj);
	}

	public void removeChild(HyperJob<V> hj) {
		assert (hj.parent == this);
		if (children.remove(hj)) {
			hj.parent = null;
			removeObservable.notify(new Pair<HyperWorkflow<F, V>, HyperJob<V>>(
					this, hj));
		}
	}

	public static <V> void removeConnectionGeneric(HyperConnection<V> cc) {
		if (cc.parent != null)
			cc.parent.removeConnection(cc);
	}

	public void removeConnection(HyperConnection<V> cc) {
		assert (cc.parent == this);
		if (connections.remove(cc)) {
			setPortBlocked(cc.getTarget(), cc.getTargetPort(), false);
			cc.parent = null;
			disconnectObservable
					.notify(new Pair<HyperWorkflow<F, V>, HyperConnection<V>>(
							this, cc));
		}

	}

	public void setAddObservable(MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperJob<V>>> addObservable) {
		this.addObservable = addObservable;
	}

	public void setConnectObservable(MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperConnection<V>>> connectObservable) {
		this.connectObservable = connectObservable;
	}

	public void setDimensions(HyperJob<V> hj, double[] d) {
		assert (children.contains(hj));

		if (d[0] != hj.dimensions[0] || d[1] != hj.dimensions[1]
				|| d[2] != hj.dimensions[2] || d[3] != hj.dimensions[3]) {
			hj.setDimensions(d);
			modifyObservable.notify(new Pair<HyperWorkflow<F, V>, HyperJob<V>>(
					this, hj));
		}
	}
	
	public void setDisconnectObservable(MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperConnection<V>>> disconnectObservable) {
		this.disconnectObservable = disconnectObservable;
	}

	public void setModifyObservable(MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperJob<V>>> modifyObservable) {
		this.modifyObservable = modifyObservable;
	}

	protected void setPortBlocked(HyperJob<V> hj, int index, boolean value) {
		if (blockedPortsMap == null)
			blockedPortsMap = computeBlockedPortsMap();
		BitSet blocked = blockedPortsMap.get(hj);
		if (blocked == null) {
			blocked = new BitSet();
			blockedPortsMap.put(hj, blocked);
		}
		blocked.set(index, value);
	}
	
	public void setRemoveObservable(MultiplexObserver<Pair<HyperWorkflow<F, V>, HyperJob<V>>> removeObservable) {
		this.removeObservable = removeObservable;
	}

	/**
	 * An intermediate representation in which some Choice nodes may have been
	 * resolved.
	 * 
	 * @author mbue
	 * 
	 */
	private class PartiallyUnfolded {
		private final Set<HyperJob<V>> remaining;
		private final LinkedList<Choice<V>> remainingOr;
		private final Map<HyperJob<V>, LinkedList<Connection<HyperJob<V>>>> forward;
		private final Map<HyperJob<V>, LinkedList<Connection<HyperJob<V>>>> backward;

		public PartiallyUnfolded() {
			/*
			 * include Choice nodes in the set of remaining nodes for later
			 * conversion
			 */
			remaining = new HashSet<HyperJob<V>>(children);
			remainingOr = new LinkedList<Choice<V>>();
			for (HyperJob<V> c : children) {
				if (c instanceof Choice<?>)
					remainingOr.add((Choice<V>) c);
			}
			forward = new HashMap<HyperJob<V>, LinkedList<Connection<HyperJob<V>>>>();
			backward = new HashMap<HyperJob<V>, LinkedList<Connection<HyperJob<V>>>>();
			for (Connection<HyperJob<V>> cc : connections) {
				// update backward map with cc
				{
					HyperJob<V> t = cc.getTarget();
					LinkedList<Connection<HyperJob<V>>> b = backward.get(t);
					if (b == null) {
						b = new LinkedList<Connection<HyperJob<V>>>();
						backward.put(t, b);
					}
					b.add(cc);
				}
				// update forward map with cc
				{
					HyperJob<V> s = cc.getSource();
					LinkedList<Connection<HyperJob<V>>> f = forward.get(s);
					if (f == null) {
						f = new LinkedList<Connection<HyperJob<V>>>();
						forward.put(s, f);
					}
					f.add(cc);
				}
			}
		}

		private PartiallyUnfolded(PartiallyUnfolded parent) {
			remaining = new HashSet<HyperJob<V>>(parent.remaining);
			remainingOr = new LinkedList<Choice<V>>(parent.remainingOr);
			forward = new HashMap<HyperJob<V>, LinkedList<Connection<HyperJob<V>>>>(
					parent.forward);
			// backward is not copied because it is never changed
			backward = parent.backward;
		}

		public boolean isFinal() {
			return remainingOr.isEmpty();
		}

		public void expand(LinkedList<PartiallyUnfolded> pipeline) {
			Choice<V> c = remainingOr.poll();
			if (c != null) {
				LinkedList<Connection<HyperJob<V>>> b = backward.get(c);
				if (b != null) {
					for (Connection<HyperJob<V>> cc : b) {
						assert (cc.getTarget() == c);
						PartiallyUnfolded newone = new PartiallyUnfolded(this);
						newone.cropOr(cc);
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
		private void cropOr(Connection<HyperJob<V>> cc) {
			HyperJob<V> t = cc.getTarget();
			assert (t instanceof Choice<?>);
			LinkedList<Connection<HyperJob<V>>> b = backward.get(t);
			assert (b != null);
			for (Connection<HyperJob<V>> cc1 : b) {
				if (cc1 != cc)
					removeConnection(cc1);
			}
		}

		/**
		 * Removes the given connection, then checks whether its source no
		 * longer has outgoing connections, in which case that is removed as
		 * well, and the removal continues with its ingoing connections.
		 * 
		 * @param cc
		 */
		private void removeConnection(Connection<HyperJob<V>> cc) {
			HyperJob<V> s = cc.getSource();
			LinkedList<Connection<HyperJob<V>>> f = forward.get(s);
			f.remove(cc);
			// retain ports; the interface of a composite job shouldn't change
			if (f.isEmpty() && !s.isInputPort()) {
				remaining.remove(s);
				// remove dangling reference to the empty list f
				forward.remove(s);
				LinkedList<Connection<HyperJob<V>>> b = backward.get(s);
				for (Connection<HyperJob<V>> cc1 : b)
					removeConnection(cc1);
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
	private static class NAryJobCounter<V> {
		private final List<Job<V>> jobs;
		private ListIterator<Job<V>> iterator;
		private Job<V> current;

		public NAryJobCounter(List<Job<V>> jobs) {
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

		public Job<V> getCurrent() {
			return current;
		}

		public boolean isReset() {
			return !iterator.hasPrevious();
		}
	}

	public List<JobWorkflow<F, V>> unfold() {
		LinkedList<JobWorkflow<F, V>> result = new LinkedList<JobWorkflow<F, V>>();
		/*
		 * step 1: unfold children separately, putting everything into a map
		 */
		Map<HyperJob<V>, NAryJobCounter<V>> damap = new HashMap<HyperJob<V>, NAryJobCounter<V>>();
		for (HyperJob<V> c : children)
			damap.put(c, new NAryJobCounter<V>(c.unfold()));
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
			Iterator<HyperJob<V>> it = p.remaining.iterator();
			int i = 0;
			while (it.hasNext()) {
				HyperJob<V> hj = it.next();
				current[i] = damap.get(hj);
				assert (current[i].isReset());
				i++;
			}
			int clength = i;
			/*
			 * go through all combinations and create the corresponding
			 * JobWorkflows
			 */
			boolean carry = clength == 0;
			while (!carry) {
				JobWorkflow<F, V> wf = new JobWorkflow<F, V>(compiler, this);
				// map remaining children
				for (HyperJob<V> j : p.remaining)
					wf.addChild(damap.get(j).getCurrent());
				// map remaining connections
				for (Connection<HyperJob<V>> cc : connections) {
					if (p.remaining.contains(cc.getSource())
							&& p.remaining.contains(cc.getTarget())) {
						JobConnection<V> ccn;
						// input port change for Choice nodes
						if (cc.getTarget() instanceof Choice<?>) {
							ccn = new JobConnection<V>(damap
									.get(cc.getSource()).getCurrent(),
									cc.getSourcePort(), damap.get(
											cc.getTarget()).getCurrent(), 0, cc);
						} else {
							ccn = new JobConnection<V>(damap
									.get(cc.getSource()).getCurrent(),
									cc.getSourcePort(), damap.get(
											cc.getTarget()).getCurrent(),
									cc.getTargetPort(), cc);
						}
						wf.addConnection(ccn);
					}
				}
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
