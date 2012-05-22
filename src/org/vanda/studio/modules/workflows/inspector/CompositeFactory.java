package org.vanda.studio.modules.workflows.inspector;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JComponent;

public class CompositeFactory<T> implements ElementEditorFactory<T> {
	private List<ElementEditorFactory<? super T>> list;
	
	public CompositeFactory() {
		list = new LinkedList<ElementEditorFactory<? super T>>();
	}
	
	public void add(ElementEditorFactory<? super T> eef) {
		list.add(eef);
	}
	
	@Override
	public JComponent createEditor(T object) {
		JComponent result = null;
		ListIterator<ElementEditorFactory<? super T>> li = list.listIterator();
		while (result == null && li.hasNext())
			result = li.next().createEditor(object);
		return result;
	}
}