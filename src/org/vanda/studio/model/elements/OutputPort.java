package org.vanda.studio.model.elements;

import java.util.Collections;
import java.util.List;

import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.Observable;

public final class OutputPort implements Element {

	private final Port port;
	private final List<Port> ports;

	public OutputPort() {
		this("output port");
	}
	
	public OutputPort(String name) {
		port = new Port(name, Types.genericType);
		ports = Collections.singletonList(port);
	}

	@Override
	public void appendActions(List<Action> as) {

	}

	@Override
	public Element clone() {
		return new OutputPort();
	}

	@Override
	public Type getFragmentType() {
		return Types.genericType;
	}

	@Override
	public String getId() {
		return "outport";
	}

	@Override
	public List<Port> getInputPorts() {
		return ports;
	}

	@Override
	public String getName() {
		return "outport[" + port.getIdentifier() + "]";
	}

	@Override
	public List<Port> getOutputPorts() {
		return Collections.emptyList();
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectOutputPortRenderer();
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
	public void visit(RepositoryItemVisitor v) {
		v.visitOutputPort(this);
	}

	@Override
	public Observable<ElementEvent> getObservable() {
		return null;
	}

}
