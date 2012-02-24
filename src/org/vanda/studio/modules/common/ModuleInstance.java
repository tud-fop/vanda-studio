/**
 * 
 */
package org.vanda.studio.modules.common;

import org.vanda.studio.model.VObject;
import org.vanda.studio.util.Observer;

/**
 * @author buechse
 * 
 */
public interface ModuleInstance<T extends VObject> {
	public void openEditor(T t);
	public Observer<VObject> getModifyObserver();
	public String getPath();
}
