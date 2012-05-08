/**
 * 
 */
package org.vanda.studio.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.MetaRepository;
import org.vanda.studio.app.Repository;
import org.vanda.studio.app.UIMode;
import org.vanda.studio.app.WindowSystem;
import org.vanda.studio.model.generation.Profile;
import org.vanda.studio.model.workflows.Compiler;
import org.vanda.studio.model.workflows.Linker;
import org.vanda.studio.model.workflows.Tool;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;

/**
 * @author buechse
 * 
 */
public final class ApplicationImpl implements Application {

	protected UIMode mode;
	protected final ArrayList<UIMode> modes;
	protected final MultiplexObserver<Application> modeObservable;
	protected final CompositeRepository<Compiler<?, ?>> compilerRepository;
	protected final CompositeRepository<Linker<?, ?, ?>> linkerRepository;
	protected final CompositeRepository<Profile> profileRepository;
	protected final CompositeRepository<Tool<?,?>> toolRepository;
	protected final MultiplexObserver<Application> shutdownObservable;
	protected final WindowSystemImpl windowSystem;

	public ApplicationImpl() {
		modes = new ArrayList<UIMode>();
		addUIModes(modes);
		mode = modes.get(0);
		modeObservable = new MultiplexObserver<Application>();
		compilerRepository = new CompositeRepository<Compiler<?,?>>();
		linkerRepository = new CompositeRepository<Linker<?,?,?>>();
		profileRepository = new CompositeRepository<Profile>();
		toolRepository = new CompositeRepository<Tool<?,?>>();
		shutdownObservable = new MultiplexObserver<Application>();
		windowSystem = new WindowSystemImpl(this);
		
		/*repository.getRemoveObservable().addObserver(
			new Observer<Tool<?,?>>() {
				@Override
				public void notify(Tool<?,?> o) {
					if (o == focus)
						focusObject(null);
				}
			});*/
		
		/*repository.getModifyObservable().addObserver(
			new Observer<Tool<?,?>>() {
				@Override
				public void notify(Tool<?,?> o) {
					if (o == focus)
						focusedObjectModifiedObservable.notify(ApplicationImpl.this);
				}
			});*/
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
	public Observable<Application> getUIModeObservable() {
		return modeObservable;
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

	@Override
	public Repository<Compiler<?, ?>> getCompilerRepository() {
		return compilerRepository;
	}

	@Override
	public MetaRepository<Compiler<?, ?>> getCompilerRR() {
		return compilerRepository;
	}

	@Override
	public Repository<Linker<?, ?, ?>> getLinkerRepository() {
		return linkerRepository;
	}

	@Override
	public MetaRepository<Linker<?, ?, ?>> getLinkerRR() {
		return linkerRepository;
	}

	@Override
	public Repository<Profile> getProfileRepository() {
		return profileRepository;
	}

	@Override
	public MetaRepository<Profile> getProfileRR() {
		return profileRepository;
	}

	@Override
	public Repository<Tool<?, ?>> getToolRepository() {
		return toolRepository;
	}

	@Override
	public MetaRepository<Tool<?, ?>> getToolRR() {
		return toolRepository;
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

