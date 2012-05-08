/**
 * 
 */
package org.vanda.studio.model.workflows;

import java.util.List;
import java.util.Map;

import org.vanda.studio.util.Action;

/**
 * 
 * @author buechse
 * 
 */
public interface ToolInstance {
	/**
	 * Append actions to a list. Do not forget to call super.
	 */
	void appendActions(List<Action> as);

	/**
	 * Used for serialization and cloning
	 */
	void loadFromMap(Map<String, Object> map);

	/**
	 * Used for serialization and cloning
	 */
	void saveToMap(Map<String, Object> map);
}
