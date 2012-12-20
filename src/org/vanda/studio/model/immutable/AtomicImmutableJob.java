package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Element;
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

	public Element getElement() {
		return element;
	}

	@Override
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
