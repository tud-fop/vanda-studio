package org.vanda.workflows.immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vanda.types.Type;
import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.elements.Element;
import org.vanda.workflows.elements.Port;

public class ImmutableJob {
	
	protected final Token address;
	private final Element element;
	
	public ImmutableJob(Token address, Element element) {
		this.address = address;
		this.element = element;
	}
	
	public void addFragmentTypeEquation(TypeChecker tc) {
		tc.addFragmentTypeEquation(element.getFragmentType());
	}

	public final Token getAddress() {
		return address;
	}
	
	public Element getElement() {
		return element;
	}

	public List<Port> getInputPorts() {
		return element.getInputPorts();
	}
	
	public Type getFragmentType() {
		return element.getFragmentType();
	}

	public List<Port> getOutputPorts() {
		return element.getOutputPorts();
	}
	
	public void typeCheck() throws Exception {
		// do nothing
	}
	
	public void appendText(ArrayList<Token> inputs, ArrayList<Token> outputs,
			StringBuilder lines, StringBuilder sections) {
		// if (!(element instanceof OutputPort || element instanceof InputPort)) {
			lines.append("  ");
			appendOutput(outputs, lines);
			lines.append(" = ");
			lines.append(element.getName());
			appendInput(inputs, lines);
			lines.append('\n');
		// }
	}
	
	public static void appendInput(List<Port> ports, Map<Port, Token> invars, StringBuilder lines) {
		lines.append('(');
		if (ports.size() != 0) {
			appendVariable(invars.get(ports.get(0)), lines);
		}
		for (int i = 1; i < ports.size(); i++) {
			lines.append(", ");
			appendVariable(invars.get(ports.get(i)), lines);
		}
		lines.append(')');
	}
	
	public static void appendInput(List<Token> inputs, StringBuilder lines) {
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

	public static void appendOutput(List<Port> ports, Map<Port, Token> outvars, StringBuilder lines) {
		if (ports.size() != 1)
			lines.append('(');
		if (ports.size() != 0) {
			appendVariable(outvars.get(ports.get(0)), lines);
		}
		for (int i = 1; i < ports.size(); i++) {
			lines.append(", ");
			appendVariable(outvars.get(ports.get(i)), lines);
		}
		if (ports.size() != 1)
			lines.append(')');		
	}

	public static void appendOutput(List<Token> outputs, StringBuilder lines) {
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
			lines.append(Integer.toString(tok.intValue()+1));
		}
	}
}
