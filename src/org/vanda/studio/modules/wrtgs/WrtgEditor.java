/**
 * 
 */
package org.vanda.studio.modules.wrtgs;

import org.vanda.studio.app.Application;
import org.vanda.studio.modules.common.Editor;


/**
 * @author buechse
 * 
 */
public class WrtgEditor implements Editor<VTreeGrammar> {
	
	protected Application app;
	
	public WrtgEditor(Application a) {
		app = a;
	}
	
	@Override
	public void open(VTreeGrammar t) {
	}
}
