package org.vanda.studio.model.elements;

import java.util.List;

import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Pair;

/**
 * Choice node. A choice node with one input port acts as identity.
 * 
 * @author buechse
 * 
 */
public final class Choice implements Element {

	private int inputs;
	private final MultiplexObserver<Pair<Element, Integer>> addPortObservable;
	private final MultiplexObserver<Pair<Element, Integer>> removePortObservable;

	public Choice() {
		this(2);
	}

	public Choice(int inputs) {
		this.inputs = inputs;
		addPortObservable = new MultiplexObserver<Pair<Element, Integer>>();
		removePortObservable = new MultiplexObserver<Pair<Element, Integer>>();
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
			addPortObservable.notify(new Pair<Element, Integer>(this, oldinputs));
			oldinputs++;
		}
		while (oldinputs > inputs) {
			oldinputs--;
			removePortObservable.notify(new Pair<Element, Integer>(this, oldinputs));
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
	public Observable<Element> getNameChangeObservable() {
		return null;
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
		v.visitChoice(this);
	}

	@Override
	public Observable<Pair<Element, Integer>> getAddInputPortObservable() {
		return addPortObservable;
	}

	@Override
	public Observable<Pair<Element, Integer>> getAddOutputPortObservable() {
		return null;
	}

	@Override
	public Observable<Pair<Element, Integer>> getRemoveInputPortObservable() {
		return removePortObservable;
	}

	@Override
	public Observable<Pair<Element, Integer>> getRemoveOutputPortObservable() {
		return null;
	}

}
