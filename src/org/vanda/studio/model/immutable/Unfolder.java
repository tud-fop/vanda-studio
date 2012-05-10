package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Choice;

public class Unfolder<F> {

	private final ImmutableWorkflow<F> parent;

	public Unfolder(ImmutableWorkflow<F> workflow) {
		parent = workflow;
	}

	/**
	 * This class facilitates the enumeration of all possible combinations of
	 * children. It keeps a list of jobs and an iterator. When a new combination
	 * is required, you call advance. If the list of jobs is exhausted, advance
	 * returns true, which means you have to carry on with the next counter.
	 * 
	 * @author mbue
	 * 
	 * @param <V>
	 */
	private static class NAryDigit<F> {
		private final List<ImmutableJob<F>> jobs;
		private ListIterator<ImmutableJob<F>> iterator;
		private ImmutableJob<F> current;

		public NAryDigit(List<ImmutableJob<F>> jobs) {
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

		public ImmutableJob<F> getCurrent() {
			return current;
		}

		public boolean isReset() {
			return !iterator.hasPrevious();
		}
	}

	public List<ImmutableWorkflow<F>> unfold() {
		LinkedList<ImmutableWorkflow<F>> result = new LinkedList<ImmutableWorkflow<F>>();
		/*
		 * step 1: unfold children separately, putting everything into a map
		 */
		ArrayList<NAryDigit<F>> counters = new ArrayList<NAryDigit<F>>();
		for (int i = 0; i < parent.children.size(); i++) {
			List<ImmutableJob<F>> js = parent.children.get(i).job.unfold();
			if (js != null)
				counters.set(i, new NAryDigit<F>(js));
		}
		/*
		 * step 2: resolve Choice nodes, pruning everything that is no longer
		 * connected
		 */
		LinkedList<PartiallyUnfolded<F>> workinglist = new LinkedList<PartiallyUnfolded<F>>();
		LinkedList<PartiallyUnfolded<F>> fynal = new LinkedList<PartiallyUnfolded<F>>();
		workinglist.add(new PartiallyUnfolded<F>(parent));
		while (!workinglist.isEmpty()) {
			PartiallyUnfolded<F> p = workinglist.pop();
			if (p.isFinal())
				fynal.add(p);
			else
				p.expand(workinglist);
		}
		/*
		 * step 3: substitute all combinations from step 1 into the results of
		 * step 2
		 */
		for (PartiallyUnfolded<F> p : fynal) {
			// since carry is true at the bottom of the loop, counters should be
			// reset
			for (int i = 0; i < counters.size(); i++) {
				assert (counters.get(i).isReset());
			}
			/*
			 * go through all combinations and create the corresponding
			 * JobWorkflows
			 */
			boolean carry = p.remaining > 0;
			while (!carry) {
				ArrayList<JobInfo<F>> children = new ArrayList<JobInfo<F>>(
						p.remaining);
				// in the following loop, assemble the current combination
				// and advance the counter(s) at the same time
				carry = true;
				for (int i = 0; i < parent.children.size(); i++) {
					if (!p.deleted.get(i)) {
						JobInfo<F> ji = parent.children.get(i);
						JobInfo<F> jinew = null;
						if (ji.job.isChoice()) {
							// substitute choice with identity
							jinew = new JobInfo<F>(new AtomicImmutableJob<F>(
									new Choice(1)), ji.address,
									new ArrayList<Integer>(1), ji.outputs,
									ji.outCount);
							jinew.inputs.set(1, p.choiceMap.get(i));
						} else {
							jinew = new JobInfo<F>(
									counters.get(i).getCurrent(), ji.address,
									ji.inputs, ji.outputs, ji.outCount);
						}
						children.add(jinew);
						if (carry)
							carry = counters.get(i).advance();
					}
				}
				ImmutableWorkflow<F> wf = new ImmutableWorkflow<F>(children, 0);
				result.add(wf);
			}
		}
		return result;
	}
}
