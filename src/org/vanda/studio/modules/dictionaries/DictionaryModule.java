/**
 * 
 */
package org.vanda.studio.modules.dictionaries;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.model.types.CompositeType;
import org.vanda.studio.util.ExceptionMessage;

/**
 * @author buechse
 * 
 */
public class DictionaryModule implements Module {

	private static final class DictionaryPreviewFactory implements
			PreviewFactory {
		
		private final Application app;
		protected HashMap<String,Dictionary> openDictionaries;

		public DictionaryPreviewFactory(Application app) {
			this.app = app;
			openDictionaries = new HashMap<String,Dictionary>();
		}

		@Override
		public JComponent createPreview(String value) {
			try {
				Dictionary dict = new Dictionary(
						new File(value).getAbsolutePath(), '\t');
				return new DictionaryView(app, dict);
			} catch (IOException e) {
				return new JEditorPane("text/text", "could not open file " + value);
			}
		}

		@Override
		public void openEditor(String value) {
			try {
				Dictionary dict = openDictionaries.get(value);
				if (dict == null) {
					dict = new Dictionary(
							new File(value).getAbsolutePath(), '\t');;
					openDictionaries.put(value, dict);
					// TODO make it possible to remove the dictionary from the map
				}
				DictionaryView dv = new DictionaryView(app, dict);
				dv.setName(value);
				app.getWindowSystem().addContentWindow(null, dv, null);
				app.getWindowSystem().focusContentWindow(dv);
				dv.requestFocusInWindow();
			}
			catch (Exception e) {
				app.sendMessage(new ExceptionMessage(e));
			}
		}

	}

	@Override
	public Object createInstance(final Application app) {
		PreviewFactory pf = new DictionaryPreviewFactory(app);
		app.registerPreviewFactory(new CompositeType("EM Steps"), pf);
		return pf;
	}

	@Override
	public String getName() {
		return "Dictionaries"; // " Module for Vanda Studio";
	}

}
