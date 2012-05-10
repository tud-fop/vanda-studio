package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Choice;
import org.vanda.studio.model.elements.Element;
import org.vanda.studio.model.elements.InputPort;
import org.vanda.studio.model.elements.OutputPort;

public class AtomicImmutableJob<F> extends ImmutableJob<F> {

	private final Element element;

	public AtomicImmutableJob(Element element) {
		this.element = element;
	}

	@Override
	public ImmutableJob<?> dereference(ListIterator<Integer> address) {
		assert (!address.hasNext());
		return this;
	}

	@Override
	public List<ImmutableJob<F>> unfold() {
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
	public void appendText(ArrayList<Integer> inputs,
			ArrayList<Integer> outputs, StringBuilder lines,
			StringBuilder sections) {
		if (!(element instanceof OutputPort || element instanceof InputPort)) {
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

}
