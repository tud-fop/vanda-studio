/**
 * 
 */
package org.vanda.studio.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.MetaRepository;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.app.SemanticsModule;
import org.vanda.studio.app.ToolFactory;
import org.vanda.studio.app.UIMode;
import org.vanda.studio.app.WindowSystem;
import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.modules.common.CompositeRepository;
import org.vanda.studio.util.ExceptionMessage;
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
	// protected final CompositeRepository<Profile> profileRepository;
	protected final HashMap<Type, PreviewFactory> previewFactories;
	protected final CompositeRepository<Tool> toolRepository;
	protected final CompositeRepository<ToolFactory> toolFactoryRepository;
	protected final MultiplexObserver<Application> shutdownObservable;
	protected final WindowSystemImpl windowSystem;
	protected final HashSet<Type> types;
	protected final Observer<Tool> typeObserver;
	protected final Properties properties;
	protected SemanticsModule semantics;
	
	protected static String PROPERTIES_FILE = System.getProperty("user.home") + "/.vanda/studio.conf";

	public ApplicationImpl() {
		this(true);
	}
	
	public ApplicationImpl(boolean gui) {
		modes = new ArrayList<UIMode>();
		addUIModes(modes);
		messageObservable = new MultiplexObserver<Message>();
		mode = modes.get(0);
		converterToolRepository = new CompositeRepository<Tool>();
		modeObservable = new MultiplexObserver<Application>();
		linkerRepository = new CompositeRepository<Linker>();
		// profileRepository = new CompositeRepository<Profile>();
		previewFactories = new HashMap<Type, PreviewFactory>();
		toolRepository = new CompositeRepository<Tool>();
		toolFactoryRepository = new CompositeRepository<ToolFactory>();
		shutdownObservable = new MultiplexObserver<Application>();
		if (gui)
			windowSystem = new WindowSystemImpl(this);
		else
			windowSystem = null;
		types = new HashSet<Type>();
		properties = new Properties();
		try {
			properties.loadFromXML(new FileInputStream(PROPERTIES_FILE));
		} catch (Exception e) {
			sendMessage(new ExceptionMessage(e));
		}
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

	// @Override
	// public MetaRepository<Profile> getProfileMetaRepository() {
	// 	return profileRepository;
	// }

	// XXX this should be done by the semanticsModule of choice
	@Override
	public MetaRepository<Tool> getToolMetaRepository() {
		return semantics.getTools();
//		return toolRepository;
	}

	@Override
	public MetaRepository<ToolFactory> getToolFactoryMetaRepository() {
		return toolFactoryRepository;
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

	@Override
	public String getProperty(String key) {
		if (!properties.contains(key)){
			String outputPath = System.getProperty("user.home")
					+ "/" + ".vanda/output/";
			String inputPath = System.getProperty("user.home")
					+ "/" + ".vanda/input/";
			if (key.equals("inputPath"))
				setProperty(key, inputPath);
			if (key.equals("outputPath"))
				setProperty(key, outputPath);
		}
		return properties.getProperty(key);
	}

	@Override
	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
		try {
			properties.storeToXML(new FileOutputStream(PROPERTIES_FILE), null);
		} catch (Exception e) {
			sendMessage(new ExceptionMessage(e));
		}
	}
	
	public PreviewFactory getPreviewFactory(Type type) {
		PreviewFactory result = previewFactories.get(type);
		if (result == null)
			result = previewFactories.get(null);
		return result;
	}

	@Override
	public void registerPreviewFactory(Type type, PreviewFactory pf) {
		previewFactories.put(type, pf);
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

	 */

	@Override
	public void setSemanticsModule(SemanticsModule mod) {
		semantics = mod;
		for (Tool t : mod.getTools().getRepository().getItems())
			typeObserver.notify(t);
	}
}
