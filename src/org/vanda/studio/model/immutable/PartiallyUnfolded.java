package org.vanda.studio.model.immutable;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * An intermediate representation in which some Choice nodes may have been
 * resolved.
 * 
 * @author mbue
 * 
 */
public final class PartiallyUnfolded<F> {
	private final ImmutableWorkflow<F> parent;
	public int remaining;
	public final BitSet deleted;
	private int position;
	// extraMap maps choice nodes (via index) to the chosen token
	public final HashMap<Integer, Integer> choiceMap;
	private final int[] outCount;

	public PartiallyUnfolded(ImmutableWorkflow<F> parent) {
		this.parent = parent;
		deleted = new BitSet();
		remaining = parent.children.size();
		position = parent.children.size();
		while (position > 0
				&& !parent.children.get(position - 1).job.isChoice())
			position--;
		choiceMap = new HashMap<Integer, Integer>();
		outCount = new int[parent.children.size()];
		for (int i = 0; i < outCount.length; i++)
			outCount[i] = parent.children.get(i).outCount;
	}

	private PartiallyUnfolded(PartiallyUnfolded<F> parent) {
		this.parent = parent.parent;
		remaining = parent.remaining;
		deleted = (BitSet) parent.deleted.clone();
		position = parent.position;
		choiceMap = new HashMap<Integer, Integer>(parent.choiceMap);
		outCount = Arrays.copyOf(parent.outCount, position);
	}

	/**
	 * True iff no choice node left to resolve.
	 * @return
	 */
	public boolean isFinal() {
		return position == 0;
	}

	/**
	 * Create all combinations possible by resolving the next Choice node.
	 * @param pipeline Where to put the new combinations
	 */
	public void expand(LinkedList<PartiallyUnfolded<F>> pipeline) {
		if (position > 0) {
			JobInfo<F> ji = parent.children.get(position - 1);
			for (int i = 0; i < ji.inputs.size(); i++) {
				if (ji.inputs.get(i) != null) {
					PartiallyUnfolded<F> newone = new PartiallyUnfolded<F>(this);
					newone.cropOr(position, i);
					newone.advance();
					pipeline.add(newone);
				}
			}
		}
	}

	/**
	 * Resolves the choice node (given by its index) position to input port i.
	 * 
	 * @param position
	 * @param i
	 */
	private void cropOr(int position, int i) {
		// First, reflect the removal of the other connections in the outcounts
		JobInfo<F> ji = parent.children.get(position - 1);
		for (int j = 0; j < ji.inputs.size(); j++) {
			Integer tok2 = ji.inputs.get(j);
			if (j != i && tok2 != null)
				outCount[parent.tokenSource[ji.inputs.get(j).intValue()]]--;
		}
		// Second, put the chosen token on record
		int pos = position - 1;
		choiceMap.put(pos, ji.inputs.get(i));
		// Third, propagate removal of unnecessary elements
		while (pos >= 0) {
			if (outCount[pos] == 0) {
				ji = parent.children.get(pos);
				for (int j = 0; j < ji.inputs.size(); j++) {
					Integer tok2 = ji.inputs.get(j);
					if (tok2 != null)
						outCount[parent.tokenSource[ji.inputs.get(j).intValue()]]--;
				}
				deleted.set(pos);
				remaining--;
			}
			pos--;
		}
	}

	/**
	 * Go to the next Choice node. Called from expand _AFTER_ cropOr because
	 * cropOr may delete Choice nodes.
	 */
	private void advance() {
		while (position > 0
				&& (!parent.children.get(position - 1).job.isChoice() || deleted
						.get(position - 1)))
			position--;
	}

}