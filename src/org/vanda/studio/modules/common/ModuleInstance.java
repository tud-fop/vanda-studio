/**
 * 
 */
package org.vanda.studio.modules.common;

import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.util.Observer;

/**
 * @author buechse
 * 
 */
public interface ModuleInstance<V, T extends Tool<V>> {
	public void openEditor(T t);
	public Observer<T> getModifyObserver();
	public String getPath();
}
