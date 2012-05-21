/**
 * 
 */
package org.vanda.studio.modules.dictionaries;

import java.io.IOException;

import org.vanda.studio.model.elements.Tool;

/**
 * @author buechse
 * 
 */


public abstract class VDictionary extends Tool {	
	public abstract Dictionary load() throws IOException;
}
