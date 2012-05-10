package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public final class ImmutableWorkflow<F> {

	final ArrayList<JobInfo<F>> children;
	final int[] tokenSource;
	private final Map<Integer, ImmutableJob<F>> deref;

	/**
	 * 
	 * @param children
	 *            topological sort of children
	 * @param maxtoken
	 *            maximum token
	 */
	public ImmutableWorkflow(ArrayList<JobInfo<F>> children, int maxtoken) {
		this.children = children;
		deref = new HashMap<Integer, ImmutableJob<F>>();
		tokenSource = new int[maxtoken];
		if (maxtoken > 0) {
			for (int i = 0; i < children.size(); i++) {
				JobInfo<F> ji = children.get(i);
				for (Integer tok : ji.outputs)
					tokenSource[tok.intValue()] = i;
				deref.put(ji.address, ji.job);
			}
		}
	}

	public ImmutableJob<?> dereference(ListIterator<Integer> address) {
		assert (address != null && address.hasNext());
		ImmutableJob<?> hj = deref.get(address.next());
		if (hj != null)
			hj = hj.dereference(address);
		return hj;
	}

	public ArrayList<JobInfo<F>> getChildren() {
		return children;
	}

	public List<ImmutableWorkflow<F>> unfold() {
		if (tokenSource.length == 0)
			return Collections.singletonList(this);
		else
			return new Unfolder<F>(this).unfold();
	}

	public void appendText(StringBuilder sections) {
		ArrayList<Integer> outputs = new ArrayList<Integer>();
		ArrayList<Integer> inputs = new ArrayList<Integer>();
		for (JobInfo<F> ji : children) {
			// FIXME: sort ports correctly
			if (ji.job.isInputPort()) {
				inputs.add(ji.outputs.get(0));
			}
			if (ji.job.isOutputPort()) {
				outputs.add(ji.inputs.get(0));

			}
		}
		StringBuilder lines = new StringBuilder();
		ImmutableJob.appendOutput(outputs, lines);
		lines.append(" = ");
		lines.append(toString());
		ImmutableJob.appendInput(inputs, lines);
		lines.append('\n');
		for (JobInfo<F> ji : children) {
			lines.append("  ");
			ji.job.appendText(ji.inputs, ji.outputs, lines, sections);
		}
		sections.append(lines);
		sections.append('\n');
	}

}
