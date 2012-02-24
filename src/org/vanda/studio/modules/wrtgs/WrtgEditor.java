/**
 * 
 */
package org.vanda.studio.modules.wrtgs;

import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.vanda.studio.app.Application;
import org.vanda.studio.modules.common.Editor;
import org.vanda.studio.util.Observer;


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
