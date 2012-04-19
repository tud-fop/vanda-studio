/**
 * 
 */
package org.vanda.studio.modules.common;

import org.vanda.studio.model.Tool;
import org.vanda.studio.util.Observer;

/**
 * @author buechse
 * 
 */
public interface ModuleInstance<T extends Tool> {
	public void openEditor(T t);
	public Observer<Tool> getModifyObserver();
	public String getPath();
}
