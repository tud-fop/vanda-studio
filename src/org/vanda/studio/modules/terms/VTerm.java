/**
 * 
 */
package org.vanda.studio.modules.terms;

import org.vanda.studio.model.Tool;

/**
 * @author buechse
 * 
 */


public interface VTerm extends Tool {
	
	Term load();
	void save(Term t);
	
}
