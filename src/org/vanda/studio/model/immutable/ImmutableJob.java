package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.util.TokenSource.Token;

public abstract class ImmutableJob {
	
	protected final Token address;
	
	public ImmutableJob(Token address) {
		this.address = address;
	}
	
	public abstract void addFragmentTypeEquation(TypeChecker tc);

	public abstract ImmutableWorkflow dereference(ListIterator<Token> path);
	
	public final Token getAddress() {
		return address;
	}

	public abstract List<Port> getInputPorts();
	
	public abstract Type getFragmentType();

	public abstract List<Port> getOutputPorts();

	public abstract boolean isChoice();

	public abstract boolean isInputPort();

	public abstract boolean isOutputPort();
	
	public abstract void typeCheck() throws Exception;
	
	/**
	 * Return null if unfold is identity (true for all AtomicImmutableJobs).
	 * 
	 * @return
	 */
	public abstract List<ImmutableJob> unfold();

	public abstract void appendText(ArrayList<Token> inputs,
			ArrayList<Token> outputs, StringBuilder lines,
			StringBuilder sections);
	
	public static void appendInput(ArrayList<Token> inputs, StringBuilder lines) {
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

	public static void appendOutput(ArrayList<Token> outputs, StringBuilder lines) {
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
	
	public static void appendVariable(Token tok, StringBuilder lines) {
		if (tok == null)
			lines.append("?");
		else {
			lines.append("x");
			lines.append(tok);
		}
	}
}
