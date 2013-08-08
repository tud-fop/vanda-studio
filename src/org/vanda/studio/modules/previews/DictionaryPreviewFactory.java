package org.vanda.studio.modules.previews;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import org.vanda.dictionaries.Dictionary;
import org.vanda.dictionaries.DictionaryView;
import org.vanda.dictionaries.DictionaryViews;
import org.vanda.dictionaries.DictionaryViews.DictionaryViewState;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.app.UIMode;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Observer;

final class DictionaryPreviewFactory implements
		PreviewFactory {

	private final Application app;
	private final HashMap<String, WeakReference<Dictionary>> openDictionaries;
	private final LinkedList<WeakReference<DictionaryView>> ds;
	private DictionaryViewState viewState;
	
	public DictionaryPreviewFactory(Application app) {
		this.app = app;
		viewState = new DictionaryViews.TableViewState();
		openDictionaries = new HashMap<String, WeakReference<Dictionary>>();
		ds = new LinkedList<WeakReference<DictionaryView>>();
		app.getUIModeObservable().addObserver(new UIObserver());
	}

	@Override
	public JComponent createPreview(String value) {
		try {
			Dictionary dict = new Dictionary(
					new File(value).getAbsolutePath(), '\t');
			DictionaryView dv = new DictionaryView(dict, viewState);
			dv.getObservable().addObserver(new Observer<DictionaryViewState> () {
				@Override
				public void notify(DictionaryViewState event) {
					viewState = event;
				}
			});
			ds.push(new WeakReference<DictionaryView>(dv));
			return dv;
		} catch (IOException e) {
			return new JEditorPane("text/text", "could not open file "
					+ value);
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
				dict = new Dictionary(new File(value).getAbsolutePath(),
						'\t');
				openDictionaries.put(value, new WeakReference<Dictionary>(
						dict));
				// TODO make it possible to remove the dictionary from the
				// map
				// since we are using a weak reference, the leak is not
				// significant
			}
			DictionaryView dv = new DictionaryView(dict, viewState);
			dv.getObservable().addObserver(new Observer<DictionaryViewState> () {
				@Override
				public void notify(DictionaryViewState event) {
					viewState = event;
				}
			});
			ds.push(new WeakReference<DictionaryView>(dv));
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
	
	private class UIObserver implements Observer<Application> {

		@Override
		public void notify(Application event) {
			ListIterator<WeakReference<DictionaryView>> li = ds.listIterator();
			while (li.hasNext()) {
				WeakReference<DictionaryView> dvref = li.next();
				DictionaryView dv = dvref.get();
				if (dv != null) {
					dv.beginUpdate();
					try {
						UIMode mode = event.getUIMode();
						dv.setLargeContent(mode.isLargeContent());
						dv.setLargeUI(mode.isLargeUI());
					} finally {
						dv.endUpdate();
					}
				}
			}
		}
		
	}

}