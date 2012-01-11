/**
 * 
 */
package org.vanda.studio.modules.dictionaries;

import java.io.IOException;

import org.vanda.studio.model.VObject;

/**
 * @author buechse
 * 
 */


public interface VDictionary extends VObject {	
	Dictionary load() throws IOException;
}
