package org.vanda.studio.model.elements;

import java.util.Collections;
import java.util.List;

import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Pair;

public final class InputPort implements Element {

	private final Port port;
	private final List<Port> ports;

	public InputPort() {
		this("input port");
	}
	
	public InputPort(String name) {
		port = new Port(name, Types.genericType);
		ports = Collections.singletonList(port);
	}

	@Override
	public void appendActions(List<Action> as) {

	}

	@Override
	public Element clone() {
		return new InputPort(port.getIdentifier());
	}

	@Override
	public Type getFragmentType() {
		return Types.genericType;
	}

	@Override
	public String getId() {
		return "inport";
	}

	@Override
	public List<Port> getInputPorts() {
		return Collections.emptyList();
	}

	@Override
	public String getName() {
		return "inport[" + port.getIdentifier() + "]";
	}

	@Override
	public List<Port> getOutputPorts() {
		return ports;
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectInputPortRenderer();
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
		return "";
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
		v.visitInputPort(this);
	}

	@Override
	public Observable<Pair<Element, Integer>> getAddInputPortObservable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable<Pair<Element, Integer>> getAddOutputPortObservable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable<Pair<Element, Integer>> getRemoveInputPortObservable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable<Pair<Element, Integer>> getRemoveOutputPortObservable() {
		// TODO Auto-generated method stub
		return null;
	}

}
