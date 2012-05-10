/**
 * 
 */
package org.vanda.studio.modules.dictionaries;

import java.util.HashMap;

import org.vanda.studio.app.Application;
import org.vanda.studio.modules.common.Editor;


/**
 * @author buechse
 * 
 */
public class DictionaryEditor implements Editor<Object, VDictionary> {
	
	protected Application app;
	protected HashMap<String,Dictionary> openDictionaries;
	
	public DictionaryEditor(Application a) {
		app = a;
		openDictionaries = new HashMap<String,Dictionary>();
	}
	
	@Override
	public void open(VDictionary d) {
		try {
			Dictionary dict = openDictionaries.get(d.getId());
			if (dict == null) {
				dict = d.load();
				openDictionaries.put(d.getId(), dict);
				// TODO make it possible to remove the dictionary from the map
			}
			DictionaryView dv = new DictionaryView(app, dict);
			app.getWindowSystem().addContentWindow("", d.getName(), null, dv, null);
			app.getWindowSystem().focusContentWindow(dv);
			dv.requestFocusInWindow();
		}
		catch (Exception e) {
			// TODO log
			e.printStackTrace();
		}
	}
	
}
