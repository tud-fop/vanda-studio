package org.vanda.studio.model.immutable;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;

import org.vanda.studio.util.Token;

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
	public final BitSet touched;
	private int position;
	// extraMap maps choice nodes (via index) to the chosen token
	public final HashMap<Integer, Object> choiceMap;
	private final int[] outCount;

	public PartiallyUnfolded(ImmutableWorkflow<F> parent) {
		this.parent = parent;
		deleted = new BitSet();
		touched = new BitSet();
		remaining = parent.children.size();
		position = parent.children.size()-1;
		while (position >= 0 && !parent.children.get(position).job.isChoice())
			position--;
		choiceMap = new HashMap<Integer, Object>();
		outCount = new int[parent.children.size()];
		for (int i = 0; i < outCount.length; i++)
			outCount[i] = parent.children.get(i).outCount;
	}

	private PartiallyUnfolded(PartiallyUnfolded<F> parent) {
		this.parent = parent.parent;
		remaining = parent.remaining;
		deleted = (BitSet) parent.deleted.clone();
		touched = (BitSet) parent.touched.clone();
		position = parent.position;
		choiceMap = new HashMap<Integer, Object>(parent.choiceMap);
		outCount = Arrays.copyOf(parent.outCount, position);
	}

	/**
	 * True iff no choice node left to resolve.
	 * 
	 * @return
	 */
	public boolean isFinal() {
		return position < 0;
	}

	/**
	 * Create all combinations possible by resolving the next Choice node.
	 * 
	 * @param pipeline
	 *            Where to put the new combinations
	 */
	public void expand(LinkedList<PartiallyUnfolded<F>> pipeline) {
		if (position >= 0) {
			JobInfo<F> ji = parent.children.get(position);
			for (int i = 0; i < ji.inputs.size(); i++) {
				if (ji.inputs.get(i) != null) {
					PartiallyUnfolded<F> newone = new PartiallyUnfolded<F>(this);
					newone.cropOr(i);
					pipeline.add(newone);
				}
			}
		}
	}

	/**
	 * Resolves the choice node at the current position to input port i, then
	 * advances to the next choice node.
	 * 
	 * @param position
	 * @param i
	 */
	private void cropOr(int i) {
		// First, reflect the removal of the other connections in the outcounts
		JobInfo<F> ji = parent.children.get(position);
		for (int j = 0; j < ji.inputs.size(); j++) {
			Object tok2 = ji.inputs.get(j);
			if (j != i && tok2 != null) {
				int src = parent.tokenSource[((Token.InternedInteger) ji.inputs.get(j)).intValue()];
				outCount[src]--;
				touched.set(src);
			}
		}
		// Second, put the chosen token on record
		choiceMap.put(position, ji.inputs.get(i));
		// Third, find next or node, propagating removal of unnecessary elements
		position--;
		while (position >= 0) {
			if (touched.get(position) && outCount[position] == 0) {
				ji = parent.children.get(position);
				for (int j = 0; j < ji.inputs.size(); j++) {
					Object tok2 = ji.inputs.get(j);
					if (tok2 != null)
						outCount[parent.tokenSource[((Token.InternedInteger) ji.inputs.get(j)).intValue()]]--;
				}
				deleted.set(position);
				remaining--;
			} else if (parent.children.get(position).job.isChoice()) {
				// ######################################
				break;
				// ######################################
			}
			position--;
		}
	}

}