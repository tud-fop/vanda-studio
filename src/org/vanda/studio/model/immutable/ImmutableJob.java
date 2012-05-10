package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public abstract class ImmutableJob<F> {

	public abstract ImmutableJob<?> dereference(ListIterator<Integer> address);

	public abstract boolean isChoice();

	public abstract boolean isInputPort();

	public abstract boolean isOutputPort();

	/**
	 * Return null if unfold is identity (true for all AtomicImmutableJobs).
	 * 
	 * @return
	 */
	public abstract List<ImmutableJob<F>> unfold();

	public abstract void appendText(ArrayList<Integer> inputs,
			ArrayList<Integer> outputs, StringBuilder lines,
			StringBuilder sections);

	public static void appendOutput(ArrayList<Integer> outputs, StringBuilder lines) {
		if (outputs.size() != 1)
			lines.append('(');
		if (outputs.size() != 0) {
			lines.append("x");
			lines.append(outputs.get(0));
		}
		for (int i = 1; i < outputs.size(); i++) {
			lines.append(", x");
			lines.append(outputs.get(i));
		}
		if (outputs.size() != 1)
			lines.append(')');		
	}
	
	public static void appendInput(ArrayList<Integer> inputs, StringBuilder lines) {
		lines.append('(');
		if (inputs.size() != 0) {
			lines.append("x");
			lines.append(inputs.get(0));
		}
		for (int i = 1; i < inputs.size(); i++) {
			lines.append(", x");
			lines.append(inputs.get(i));
		}
		lines.append(')');
	}
}
