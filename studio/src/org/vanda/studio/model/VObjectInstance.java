/**
 * 
 */
package org.vanda.studio.model;

import java.util.List;
import java.util.Map;

/**
 * TODO add code generation
 *
 * @author buechse
 * 
 */
public interface VObjectInstance {
	/**
	 * Append actions to a list. Do not forget to call super.
	 */
	void appendActions(List<Action> as);
	
	/**
	 * Used for serialization and cloning
	 */
	void loadFromMap(Map<String,Object> map);
	
	/**
	 * Used for serialization and cloning
	 */
	void saveToMap(Map<String,Object> map);
}
