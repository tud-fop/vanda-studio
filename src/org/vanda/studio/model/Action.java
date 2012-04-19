/**
 * 
 */
package org.vanda.studio.model;


/**
 * @author buechse
 *
 */
public interface Action {
	
	String getName();
	
	void invoke();
	
	//public T getObject();
	//
	//
	//public static interface VOEditorFactory<T extends VObject> {
	//	
	//	public VOEditor<T> createEditor(Application app, T vObject, String id);
	//
	//}

}
