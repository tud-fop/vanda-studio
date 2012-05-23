package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Choice;
import org.vanda.studio.model.elements.Element;
import org.vanda.studio.model.elements.InputPort;
import org.vanda.studio.model.elements.OutputPort;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.util.TokenSource.Token;

public final class AtomicImmutableJob extends ImmutableJob {

	private final Element element;

	public AtomicImmutableJob(Token address, Element element) {
		super(address);
		this.element = element;
	}

	@Override
	public ImmutableWorkflow dereference(ListIterator<Token> path) {
		return null;
	}

	@Override
	public List<ImmutableJob> unfold() {
		return null;
	}

	public Element getElement() {
		return element;
	}

	@Override
	public boolean isChoice() {
		return element instanceof Choice;
	}

	@Override
	public void appendText(ArrayList<Token> inputs, ArrayList<Token> outputs,
			StringBuilder lines, StringBuilder sections) {
		if (!(element instanceof OutputPort || element instanceof InputPort)) {
			lines.append("  ");
			appendOutput(outputs, lines);
			lines.append(" = ");
			lines.append(element.getName());
			appendInput(inputs, lines);
			lines.append('\n');
		}
	}

	@Override
	public boolean isInputPort() {
		return element instanceof InputPort;
	}

	@Override
	public boolean isOutputPort() {
		return element instanceof OutputPort;
	}

	@Override
	public List<Port> getInputPorts() {
		return element.getInputPorts();
	}

	@Override
	public List<Port> getOutputPorts() {
		return element.getOutputPorts();
	}

	@Override
	public Type getFragmentType() {
		return element.getFragmentType();
	}

	@Override
	public void addFragmentTypeEquation(TypeChecker tc) {
		tc.addFragmentTypeEquation(element.getFragmentType());
	}

	@Override
	public void typeCheck() throws Exception {
		// do nothing
	}

}
