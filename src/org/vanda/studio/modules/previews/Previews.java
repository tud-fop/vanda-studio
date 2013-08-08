package org.vanda.studio.modules.previews;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.UIMode;
import org.vanda.util.Observer;

public class Previews {

	public interface Preview {
		void beginUpdate();
		void endUpdate();
		void setLargeContent(boolean mode);
		void setLargeUI(boolean mode);
	}
	
	public static class UIObserver implements Observer<Application> {
		final List<WeakReference<Preview>> previews;
		public UIObserver(List<WeakReference<Preview>> previews) {
			this.previews = previews;
		}
		
		@Override
		public void notify(Application app) {
			ListIterator<WeakReference<Preview>> li = previews.listIterator();
			while (li.hasNext()) {
				WeakReference<Preview> pRef = li.next();
				Preview p = pRef.get();
				if (p != null) {
					p.beginUpdate();
					try {
						UIMode mode = app.getUIMode();
						p.setLargeContent(mode.isLargeContent());
						p.setLargeUI(mode.isLargeUI());
					} finally {
						p.endUpdate();
					}
				}
			}			
		}
		
	}
}
