/**
 * 
 */
package org.vanda.studio.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.UIMode;
import org.vanda.studio.app.WindowSystem;
import org.vanda.studio.model.VObject;
import org.vanda.studio.model.Repository;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;

/**
 * @author buechse
 * 
 */
public class ApplicationImpl implements Application {

	protected UIMode mode;
	protected ArrayList<UIMode> modes;
	protected MultiplexObserver<Application> modeObservable;
	protected VObject focus;
	protected MultiplexObserver<Application> focusChangeObservable;
	protected MultiplexObserver<Application> focusedObjectModifiedObservable;
	protected CompositeRepository<VObject> repository;
	protected MultiplexObserver<Application> shutdownObservable;
	protected WindowSystemImpl windowSystem;

	public ApplicationImpl() {
		modes = new ArrayList<UIMode>();
		addUIModes(modes);
		mode = modes.get(0);
		modeObservable = new MultiplexObserver<Application>();
		focusChangeObservable = new MultiplexObserver<Application>();
		focusedObjectModifiedObservable = new MultiplexObserver<Application>();
		repository = new CompositeRepository<VObject>();
		shutdownObservable = new MultiplexObserver<Application>();
		windowSystem = new WindowSystemImpl(this);
		
		repository.getRemoveObservable().addObserver(
			new Observer<VObject>() {
				@Override
				public void notify(VObject o) {
					if (o == focus)
						focusObject(null);
				}
			});
		
		repository.getModifyObservable().addObserver(
			new Observer<VObject>() {
				@Override
				public void notify(VObject o) {
					if (o == focus)
						focusedObjectModifiedObservable.notify(ApplicationImpl.this);
				}
			});
	}

	@Override
	public void shutdown() {
		shutdownObservable.notify(this);
	}

	@Override
	public String createUniqueId() {
		return UUID.randomUUID().toString().toUpperCase();
	}

	@Override
	public void addRepository(Repository<VObject> r) {
		repository.addRepository(r);
	}

	@Override
	public void focusObject(VObject o) {
		if (o != focus) {
			focus = o;
			focusedObjectModifiedObservable.notify(this);
		}
	}
	
	@Override
	public Repository<VObject> getGlobalRepository() {
		return repository;
	}
	
	@Override
	public Observable<Application> getUIModeObservable() {
		return modeObservable;
	}

	@Override
	public Observable<Application> getFocusChangeObservable() {
		return focusChangeObservable;
	}
	
	@Override
	public Observable<Application> getFocusedObjectModifiedObservable() {
		return focusedObjectModifiedObservable;
	}
	
	@Override
	public VObject getFocusedObject() {
		return focus;
	}
	
	@Override
	public Observable<Application> getShutdownObservable() {
		return shutdownObservable;
	}

	@Override
	public UIMode getUIMode() {
		return mode;
	}

	@Override
	public Collection<UIMode> getUIModes() {
		return modes;
	}

	@Override
	public void removeRepository(Repository<VObject> r) {
		repository.removeRepository(r);
	}

	@Override
	public void setUIMode(UIMode m) {
		if (mode != m && modes.contains(m)) {
			mode = m;
			modeObservable.notify(this);		
		}
	}
	
	@Override
	public WindowSystem getWindowSystem() {
		return windowSystem;
	}
	
	protected static void addUIModes(Collection<UIMode> modes) {
		modes.add(
			new UIMode() {
				@Override	public String getName() { return "Normal Mode"; }
				@Override	public boolean isLargeContent() { return false; }
				@Override	public boolean isLargeUI() { return false; }
			});
		modes.add(
			new UIMode() {
				@Override	public String getName() { return "Beamer Mode";	}
				@Override	public boolean isLargeContent() { return true; }
				@Override	public boolean isLargeUI() { return false; }
			});
		modes.add(
			new UIMode() {
				@Override	public String getName() { return "Tablet Mode";	}
				@Override	public boolean isLargeContent() { return true; }
				@Override	public boolean isLargeUI() { return true; }
			});
	}
}



	/*
	@Override
	public <T extends VObject>
	void addAction(Class<? extends T> c, Action<T> a) {
		if (c == null || a == null)
			throw IllegalArgumentException("Class or action cannot be null");
		
		Set<Action<? extends VObject>> as = actions.get(c);
		
		if (as == null) {
			as = new HashSet<Action<? extends VObject>>();
			as.add(a);
			actions.put(c, as);
		}
		else {
			if (!as.add(a))
				throw new UnsupportedOperationException("cannot add action twice");
		}
	}
	 */

