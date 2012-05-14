package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Port;

public abstract class ImmutableJob<F> {

	public abstract ImmutableJob<?> dereference(ListIterator<Integer> address);

	public abstract List<Port> getInputPorts();

	public abstract List<Port> getOutputPorts();

	public abstract boolean isChoice();

	public abstract boolean isInputPort();

	public abstract boolean isOutputPort();

	/**
	 * Return null if unfold is identity (true for all AtomicImmutableJobs).
	 * 
	 * @return
	 */
	public abstract List<ImmutableJob<F>> unfold();

	public abstract void appendText(ArrayList<Object> inputs,
			ArrayList<Object> outputs, StringBuilder lines,
			StringBuilder sections);
	
	public static void appendInput(ArrayList<Object> inputs, StringBuilder lines) {
		lines.append('(');
		if (inputs.size() != 0) {
			appendVariable(inputs.get(0), lines);
		}
		for (int i = 1; i < inputs.size(); i++) {
			lines.append(", ");
			appendVariable(inputs.get(i), lines);
		}
		lines.append(')');
	}

	public static void appendOutput(ArrayList<Object> outputs, StringBuilder lines) {
		if (outputs.size() != 1)
			lines.append('(');
		if (outputs.size() != 0) {
			appendVariable(outputs.get(0), lines);
		}
		for (int i = 1; i < outputs.size(); i++) {
			lines.append(", ");
			appendVariable(outputs.get(i), lines);
		}
		if (outputs.size() != 1)
			lines.append(')');		
	}
	
	public static void appendVariable(Object tok, StringBuilder lines) {
		if (tok == null)
			lines.append("?");
		else {
			lines.append("x");
			lines.append(tok);
		}
	}
}
