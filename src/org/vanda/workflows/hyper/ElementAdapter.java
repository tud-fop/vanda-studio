package org.vanda.workflows.hyper;

import java.util.List;

import org.vanda.types.Type;
import org.vanda.util.Observable;
import org.vanda.util.RepositoryItem;
import org.vanda.workflows.elements.ElementReturnVisitor;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.RendererAssortment;
import org.vanda.workflows.hyper.ElementAdapters.*;

public interface ElementAdapter extends RepositoryItem {
	
	public Type getFragmentType();

	public List<Port> getInputPorts();
	
	public List<Port> getOutputPorts();
	
	/**
	 * may return null if immutable
	 * @return
	 */
	public Observable<ElementAdapterEvent<ElementAdapter>> getObservable();
	
	public void rebind();

	public <R> R selectRenderer(RendererAssortment<R> ra);

	public void visit(ElementVisitor v);
	
	public <R> R visitReturn(ElementReturnVisitor<R> v);

}
