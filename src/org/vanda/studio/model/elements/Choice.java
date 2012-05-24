package org.vanda.studio.model.elements;

import java.util.List;

import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;

/**
 * Choice node. A choice node with one input port acts as identity.
 * 
 * @author buechse
 * 
 */
public final class Choice implements Element {

	private int inputs;
	private final MultiplexObserver<ElementEvent> observable;

	public Choice() {
		this(2);
	}

	public Choice(int inputs) {
		this.inputs = inputs;
		observable = new MultiplexObserver<ElementEvent>();
	}

	@Override
	public Element clone() {
		return new Choice(inputs);
	}

	@Override
	public List<Port> getInputPorts() {
		return Ports.getChoiceInputPorts(inputs);
	}

	@Override
	public List<Port> getOutputPorts() {
		return Ports.choiceOutputs;
	}

	public void setInputPorts(int inputs) {
		int oldinputs = this.inputs;
		this.inputs = inputs;
		while (oldinputs < inputs) {
			observable.notify(new Elements.InputPortAddEvent(this, oldinputs));
			oldinputs++;
		}
		while (oldinputs > inputs) {
			oldinputs--;
			observable.notify(new Elements.InputPortRemoveEvent(this, oldinputs));
		}
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectOrRenderer();
	}

	@Override
	public String getName() {
		return "CHOOSE";
	}

	@Override
	public void appendActions(List<Action> as) {
		// do nothing
	}

	@Override
	public Type getFragmentType() {
		return Types.genericType;
	}

	@Override
	public String getId() {
		return "OR";
	}

	@Override
	public String getCategory() {
		return "basics";
	}

	@Override
	public String getContact() {
		return "Vanda Studio Team";
	}

	@Override
	public String getDescription() {
		return "A CHOOSE node in a hyperworkflow determines several "
				+ "possibilities of generating workflows. Each possibility "
				+ "corresponds to choosing one of the incoming connections.";
	}

	@Override
	public String getVersion() {
		return "n/a";
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
		v.visitChoice(this);
	}

	@Override
	public Observable<ElementEvent> getObservable() {
		return observable;
	}

}
