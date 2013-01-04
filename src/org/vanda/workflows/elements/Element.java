package org.vanda.workflows.elements;

import java.util.List;
import java.util.Set;

import org.vanda.types.Type;
import org.vanda.util.HasActions;
import org.vanda.util.Observable;

public interface Element extends RepositoryItem, HasActions, Cloneable {
	
	public static interface ElementListener {
		// removed: see older versions
		// void inputPortAdded(Element e, int index);
		// void inputPortRemoved(Element e, int index);
		void propertyChanged(Element e);
	}
	
	public static interface ElementEvent {
		void doNotify(ElementListener el);
	}
	
	public Element clone() throws CloneNotSupportedException;
	
	public Type getFragmentType();

	public List<Port> getInputPorts();
	
	public List<Port> getOutputPorts();
	
	/**
	 * may return null if immutable
	 * @return
	 */
	public Observable<ElementEvent> getObservable();

	public abstract <R> R selectRenderer(RendererAssortment<R> ra);

	public Set<String> getImports();
}
