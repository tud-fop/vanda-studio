package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.util.TokenSource.Token;

public class Unfolder {

	private final ImmutableWorkflow parent;

	public Unfolder(ImmutableWorkflow workflow) {
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
	private static class NAryDigit {
		private final List<ImmutableJob> jobs;
		private ListIterator<ImmutableJob> iterator;
		private ImmutableJob current;
		private boolean reset;

		public NAryDigit(List<ImmutableJob> jobs) {
			assert (!jobs.isEmpty());
			this.jobs = jobs;
			iterator = jobs.listIterator();
			reset = true;
			current = iterator.next();
		}

		/**
		 * Advances to the next job. Returns true if carry is needed.
		 * 
		 * @return
		 */
		public boolean advance() {
			boolean carry = !iterator.hasNext();
			if (carry) {
				iterator = jobs.listIterator();
				reset = true;
			} else
				reset = false;
			current = iterator.next();
			return carry;
		}

		public ImmutableJob getCurrent() {
			return current;
		}

		public boolean isReset() {
			return reset;
		}
	}

	public List<ImmutableWorkflow> unfold() {
		LinkedList<ImmutableWorkflow> result = new LinkedList<ImmutableWorkflow>();
		/*
		 * step 1: unfold children separately, putting everything into a map
		 */
		ArrayList<NAryDigit> counters = new ArrayList<NAryDigit>(
				parent.children.size());
		for (int i = 0; i < parent.children.size(); i++) {
			List<ImmutableJob> js = parent.children.get(i).job.unfold();
			if (js != null) {
				if (js.size() == 0) {
					List<ImmutableWorkflow> el = Collections.emptyList();
					// ######################
					return el;
					// ######################
				}
				counters.add(new NAryDigit(js));
			} else
				counters.add(null);
		}
		/*
		 * step 2: resolve Choice nodes, pruning everything that is no longer
		 * connected
		 */
		LinkedList<PartiallyUnfolded> workinglist = new LinkedList<PartiallyUnfolded>();
		LinkedList<PartiallyUnfolded> fynal = new LinkedList<PartiallyUnfolded>();
		workinglist.add(new PartiallyUnfolded(parent));
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
		for (PartiallyUnfolded p : fynal) {
			// since carry is true at the bottom of the loop, counters should be
			// reset
			for (int i = 0; i < counters.size(); i++) {
				assert (counters.get(i) == null || counters.get(i).isReset());
			}
			/*
			 * go through all combinations and create the corresponding
			 * JobWorkflows
			 */
			// System.out.println(p.remaining);
			boolean carry = false;
			// carry = p.remaining == 0; THIS WAS WRONG (it missed the empty wf)
			while (!carry) {
				ArrayList<JobInfo> children = new ArrayList<JobInfo>(
						p.remaining);
				// in the following loop, assemble the current combination
				// and advance the counter(s) at the same time
				carry = true;
				for (int i = 0; i < parent.children.size(); i++) {
					if (!p.deleted.get(i)) {
						JobInfo ji = parent.children.get(i);
						JobInfo jinew = null;
						if (ji.job.isChoice()) {
							// substitute choice with identity
							/*
							 * jinew = new JobInfo<F>(new AtomicImmutableJob<F>(
							 * new Choice(1)), ji.address, new
							 * ArrayList<Object>(1), ji.outputs, ji.outCount);
							 * jinew.inputs.add(p.choiceMap.get(i));
							 */
							ArrayList<Token> inputs = new ArrayList<Token>(
									ji.inputs.size());
							for (int j = 0; j < ji.inputs.size(); j++)
								if (j == p.choiceMap.get(i))
									inputs.add(ji.inputs.get(j));
								else
									inputs.add(null);
							jinew = new JobInfo(ji.job, ji.address, inputs,
									ji.outputs, ji.outCount);
						} else if (counters.get(i) != null) {
							jinew = new JobInfo(counters.get(i).getCurrent(),
									ji.address, ji.inputs, ji.outputs,
									ji.outCount);
						} else {
							jinew = ji;
						}
						children.add(jinew);
						if (carry && counters.get(i) != null)
							carry = counters.get(i).advance();
					}
				}
				ImmutableWorkflow wf = new ImmutableWorkflow(parent.name,
						children, parent.variableSource, 0);
				result.add(wf);
			}
		}
		return result;
	}
}
