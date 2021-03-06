package org.vanda.studio.modules.previews;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import org.vanda.dictionaries.Dictionary;
import org.vanda.dictionaries.DictionaryView;
import org.vanda.dictionaries.DictionaryViews;
import org.vanda.dictionaries.DictionaryViews.MutableDictionaryViewState;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.modules.previews.Previews.Preview;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Observer;

final class DictionaryPreviewFactory implements PreviewFactory {
	private static class DictionaryPreview extends DictionaryView implements Previews.Preview {
		private static final long serialVersionUID = -3571292329987894738L;

		public DictionaryPreview(Dictionary d, MutableDictionaryViewState viewState_) {
			super(d, viewState_);
		}

		@Override
		public void beginUpdate() {
			super.beginUpdate();
		}

		@Override
		public void endUpdate() {
			super.endUpdate();			
		}

		@Override
		public void setLargeContent(boolean mode) {
			super.setLargeContent(mode);
		}

		@Override
		public void setLargeUI(boolean mode) {
			super.setLargeUI(mode);			
		}
		
	}
	
	private final Application app;
	private final HashMap<String, WeakReference<Dictionary>> openDictionaries;
	private final LinkedList<WeakReference<Preview>> ds;
	private MutableDictionaryViewState viewState;
	private Observer<Application> uiModeObserver;

	public DictionaryPreviewFactory(Application app) {
		this.app = app;
		viewState = new MutableDictionaryViewState(); 
		viewState.value = new DictionaryViews.TableViewState();
		openDictionaries = new HashMap<String, WeakReference<Dictionary>>();
		ds = new LinkedList<WeakReference<Preview>>();
		uiModeObserver = new Previews.UIObserver(ds);
		app.getUIModeObservable().addObserver(uiModeObserver);
	}

	@Override
	public JComponent createPreview(String value) {
		try {
			Dictionary dict = new Dictionary(new File(value).getAbsolutePath(), '\t');
			DictionaryPreview dv = new DictionaryPreview(dict, viewState);
			dv.setLargeContent(app.getUIMode().isLargeContent());
			dv.setLargeUI(app.getUIMode().isLargeUI());
			ds.push(new WeakReference<Preview>(dv));
			return dv;
		} catch (IOException e) {
			return new JEditorPane("text/text", "could not open file " + value);
		}
	}

	@Override
	public void openEditor(String value) {
		try {
			Dictionary dict = null;
			WeakReference<Dictionary> dictref = openDictionaries.get(value);
			if (dictref != null)
				dict = dictref.get();
			if (dict == null) {
				dict = new Dictionary(new File(value).getAbsolutePath(), '\t');
				openDictionaries.put(value, new WeakReference<Dictionary>(dict));
				// TODO make it possible to remove the dictionary from the
				// map
				// since we are using a weak reference, the leak is not
				// significant
			}
			DictionaryPreview dv = new DictionaryPreview(dict, viewState);
			dv.setLargeContent(app.getUIMode().isLargeContent());
			dv.setLargeUI(app.getUIMode().isLargeUI());
			ds.push(new WeakReference<Preview>(dv));
			dv.setName(value);
			app.getWindowSystem().addContentWindow(null, dv, null);
			app.getWindowSystem().focusContentWindow(dv);
			dv.requestFocusInWindow();
		} catch (Exception e) {
			app.sendMessage(new ExceptionMessage(e));
		}
	}

	@Override
	public JComponent createSmallPreview(String absolutePath) {
		return createPreview(absolutePath);
	}
}