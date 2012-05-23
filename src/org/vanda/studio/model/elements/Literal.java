package org.vanda.studio.model.elements;

import java.util.Collections;
import java.util.List;

import org.vanda.studio.model.types.Type;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;

public final class Literal implements Element {
	
	private String type;
	private String value;
	private final MultiplexObserver<Element> nameChangeObservable;
	private final MultiplexObserver<Element> portsChangeObservable;

	public Literal(String type, String value) {
		this.type = type;
		this.value = value;
		nameChangeObservable = new MultiplexObserver<Element>();
		portsChangeObservable = new MultiplexObserver<Element>();
	}

	@Override
	public void appendActions(List<Action> as) {

	}
	
	@Override
	public Element clone() {
		return new Literal(type, value);
	}

	@Override
	public Type getFragmentType() {
		return Ports.typeVariable;
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
		return Ports.literalOutputs;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		if (!type.equals(this.type)) {
			this.type = type;
			portsChangeObservable.notify(this);
		}
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if (!value.equals(this.value)) {
			this.value = value;
			nameChangeObservable.notify(this);
		}
	}

	@Override
	public Observable<Element> getNameChangeObservable() {
		return nameChangeObservable;
	}

	@Override
	public Observable<Element> getPortsChangeObservable() {
		return portsChangeObservable;
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
		v.visitLiteral(this);
	}

}
