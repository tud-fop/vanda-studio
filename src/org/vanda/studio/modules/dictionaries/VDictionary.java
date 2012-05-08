/**
 * 
 */
package org.vanda.studio.modules.dictionaries;

import java.io.IOException;

import org.vanda.studio.model.generation.ShellView;
import org.vanda.studio.model.workflows.Tool;
import org.vanda.studio.model.workflows.ToolInstance;

/**
 * @author buechse
 * 
 */


public interface VDictionary extends Tool<ShellView, ToolInstance> {	
	Dictionary load() throws IOException;
}
