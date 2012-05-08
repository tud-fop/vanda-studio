/**
 * 
 */
package org.vanda.studio.modules.common;

import org.vanda.studio.model.workflows.Tool;
import org.vanda.studio.model.workflows.ToolInstance;
import org.vanda.studio.util.Observer;

/**
 * @author buechse
 * 
 */
public interface ModuleInstance<V, I extends ToolInstance, T extends Tool<V, I>> {
	public void openEditor(T t);
	public Observer<T> getModifyObserver();
	public String getPath();
}
