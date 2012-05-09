/**
 * 
 */
package org.vanda.studio.modules.dictionaries;

import java.io.IOException;

import org.vanda.studio.model.generation.ShellView;
import org.vanda.studio.model.workflows.Tool;

/**
 * @author buechse
 * 
 */


public interface VDictionary extends Tool<ShellView> {	
	Dictionary load() throws IOException;
}
