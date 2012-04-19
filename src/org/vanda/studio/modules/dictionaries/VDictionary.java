/**
 * 
 */
package org.vanda.studio.modules.dictionaries;

import java.io.IOException;

import org.vanda.studio.model.Tool;

/**
 * @author buechse
 * 
 */


public interface VDictionary extends Tool {	
	Dictionary load() throws IOException;
}
