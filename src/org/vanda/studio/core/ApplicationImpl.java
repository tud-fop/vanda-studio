/**
 * 
 */
package org.vanda.studio.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.MetaRepository;
import org.vanda.studio.app.Profile;
import org.vanda.studio.app.UIMode;
import org.vanda.studio.app.WindowSystem;
import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.TypeVariable;
import org.vanda.studio.modules.common.CompositeRepository;
import org.vanda.studio.util.Message;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;

/**
 * @author buechse
 * 
 */
public final class ApplicationImpl implements Application {

	protected UIMode mode;
	protected final ArrayList<UIMode> modes;
	protected final CompositeRepository<Tool> converterToolRepository;
	protected final MultiplexObserver<Message> messageObservable;
	protected final MultiplexObserver<Application> modeObservable;
	protected final CompositeRepository<Linker> linkerRepository;
	protected final CompositeRepository<Profile> profileRepository;
	protected final CompositeRepository<Tool> toolRepository;
	protected final MultiplexObserver<Application> shutdownObservable;
	protected final WindowSystemImpl windowSystem;
	protected final HashSet<Type> types;
	protected final Observer<Tool> typeObserver;

	public ApplicationImpl() {
		modes = new ArrayList<UIMode>();
		addUIModes(modes);
		messageObservable = new MultiplexObserver<Message>();
		mode = modes.get(0);
		converterToolRepository = new CompositeRepository<Tool>();
		modeObservable = new MultiplexObserver<Application>();
		linkerRepository = new CompositeRepository<Linker>();
		profileRepository = new CompositeRepository<Profile>();
		toolRepository = new CompositeRepository<Tool>();
		shutdownObservable = new MultiplexObserver<Application>();
		windowSystem = new WindowSystemImpl(this);
		types = new HashSet<Type>();
		typeObserver = new Observer<Tool>() {

			@Override
			public void notify(Tool event) {
				for (Port p : event.getInputPorts()) {
					Type t = p.getType();
					t.getSubTypes(types);
				}
				for (Port p : event.getOutputPorts()) {
					Type t = p.getType();
					t.getSubTypes(types);
				}
			}
			
		};
		
		converterToolRepository.getAddObservable().addObserver(typeObserver);
		toolRepository.getAddObservable().addObserver(typeObserver);
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
	public MetaRepository<Tool> getConverterToolMetaRepository() {
		return converterToolRepository;
	}

	@Override
	public MetaRepository<Linker> getLinkerMetaRepository() {
		return linkerRepository;
	}

	@Override
	public MetaRepository<Profile> getProfileMetaRepository() {
		return profileRepository;
	}

	@Override
	public MetaRepository<Tool> getToolMetaRepository() {
		return toolRepository;
	}

	@Override
	public Observable<Message> getMessageObservable() {
		return messageObservable;
	}

	@Override
	public void sendMessage(Message m) {
		messageObservable.notify(m);
	}

	@Override
	public Set<Type> getTypes() {
		return types;
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

