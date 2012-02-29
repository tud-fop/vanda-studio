/**
 * 
 */
package org.vanda.studio.modules.terms;

import org.vanda.studio.model.VObject;

/**
 * @author buechse
 * 
 */


public interface VTerm extends VObject {
	
	Term load();
	void save(Term t);
	
}
