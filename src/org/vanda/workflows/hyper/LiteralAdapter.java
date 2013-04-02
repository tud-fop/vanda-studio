package org.vanda.workflows.hyper;

import java.util.Collections;
import java.util.List;

import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.ElementReturnVisitor;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.RendererAssortment;
import org.vanda.workflows.elements.Elements.*;
import org.vanda.workflows.hyper.ElementAdapters.*;

public final class LiteralAdapter implements ElementAdapter, ElementListener<Literal> {
	
	private Literal lit;
	private Port port;
	private List<Port> ports;
	private final MultiplexObserver<ElementAdapterEvent<ElementAdapter>> observable;
	
	public LiteralAdapter(Literal lit) {
		this.lit = lit;
		observable = new MultiplexObserver<ElementAdapterEvent<ElementAdapter>>();
		port = new Port("literal", lit.getType());
		ports = Collections.singletonList(port);		
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
		return lit.getValue();
	}

	@Override
	public List<Port> getOutputPorts() {
		return ports;
	}
	@Override
	public String getVersion() {
		return "n/a";
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectLiteralRenderer();
	}

	@Override
	public Observable<ElementAdapterEvent<ElementAdapter>> getObservable() {
		return observable;
	}

	@Override
	public void visit(ElementVisitor v) {
		v.visitLiteral(lit);
	}

	@Override
	public <R> R visitReturn(ElementReturnVisitor<R> v) {
		return v.visitLiteral(lit);
	}

	@Override
	public void rebind() {
		// TODO Auto-generated method stub
		lit.getObservable().addObserver(new Observer<ElementEvent<Literal>>() {
				@Override
				public void notify(ElementEvent<Literal> event) {
					event.doNotify(LiteralAdapter.this);
				}
			});

	}

	@Override
	public void propertyChanged(Literal e) {
		port.setType(lit.getType());
		observable.notify(new PropertyChangedEvent<ElementAdapter>(this));
	}

}
