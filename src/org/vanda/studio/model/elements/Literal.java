package org.vanda.studio.model.elements;

import java.util.Collections;
import java.util.List;

import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;

public final class Literal implements Element {
	
	private Port port;
	private List<Port> ports;
	// private String type;
	private String value;
	private final MultiplexObserver<ElementEvent> observable;

	public Literal(Type type, String value) {
		port = new Port("literal", type);
		ports = Collections.singletonList(port);
		// this.type = type;
		this.value = value;
		observable = new MultiplexObserver<ElementEvent>();
	}

	@Override
	public void appendActions(List<Action> as) {

	}
	
	@Override
	public Element clone() {
		return new Literal(port.getType(), value);
	}

	@Override
	public Type getFragmentType() {
		return Types.genericType;
	}

	@Override
	public String getId() {
		return "literal";
	}

	@Override
	public List<Port> getInputPorts() {
		return Collections.emptyList();
	}

	@Override
	public String getName() {
		return "literal["+value+"]";
	}

	@Override
	public List<Port> getOutputPorts() {
		return ports;
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectLiteralRenderer();
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

	public Type getType() {
		return port.getType();
	}

	public void setType(Type type) {
		if (!type.equals(port.getType())) {
			port.setType(type);
			observable.notify(new Elements.PropertyChangeEvent(this));
		}
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if (!value.equals(this.value)) {
			this.value = value;
			observable.notify(new Elements.PropertyChangeEvent(this));
		}
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
		v.visitLiteral(this);
	}

	@Override
	public Observable<ElementEvent> getObservable() {
		return observable;
	}

}
