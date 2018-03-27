/**
 * 
 */
package org.vanda.studio.app;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.swing.UIManager;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.Element;
import org.vanda.datasources.RootDataSource;
import org.vanda.studio.core.ModuleManager;
import org.vanda.types.Type;
import org.vanda.util.CompositeRepository;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Message;
import org.vanda.util.MetaRepository;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;

/**
 * Root node of the Vanda Studio Application Object Model.
 * 
 * @author buechse
 */
public class Application {

	protected UIMode mode;
	protected final ArrayList<UIMode> modes;
	// outdated protected final CompositeRepository<Tool>
	// converterToolRepository;
	// to keep a reference to the ModuleManager 
	protected ModuleManager moduleManager;
	protected final MultiplexObserver<Message> messageObservable;
	protected final MultiplexObserver<Application> modeObservable;
	// protected final CompositeRepository<Profile> profileRepository;
	protected final HashMap<Type, PreviewFactory> previewFactories;
	protected final CompositeRepository<Tool> toolRepository;
	protected final RootDataSource rootDataSource;
	protected final MultiplexObserver<Application> shutdownObservable;
	protected final WindowSystem windowSystem;
	protected final HashSet<Type> types;
	// protected final Observer<ToolInterface> tiObserver;
	protected final Observer<Tool> typeObserver;
	protected final Properties properties;

	protected static String PROPERTIES_FILE = System.getProperty("user.home") + "/.vanda/studio.conf";

	public Application() {
		this(true);
	}

	public Application(boolean gui) {
		modes = new ArrayList<UIMode>();
		addUIModes(modes);
		messageObservable = new MultiplexObserver<Message>();
		mode = modes.get(0);
		// converterToolRepository = new CompositeRepository<Tool>();
		modeObservable = new MultiplexObserver<Application>();
		// profileRepository = new CompositeRepository<Profile>();
		previewFactories = new HashMap<Type, PreviewFactory>();
		toolRepository = new CompositeRepository<Tool>();
		rootDataSource = new RootDataSource(new HashMap<String, DataSource>());
		shutdownObservable = new MultiplexObserver<Application>();
		if (gui)
			windowSystem = new WindowSystem(this);
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

		/*
		 * tiObserver = new Observer<ToolInterface>() {
		 * 
		 * @Override public void notify(ToolInterface ti) { for (Tool t :
		 * ti.getTools().getItems()) typeObserver.notify(t); }
		 * 
		 * };
		 */

		// converterToolRepository.getAddObservable().addObserver(typeObserver);
		toolRepository.getAddObservable().addObserver(typeObserver);
		
		// Register a user defined font
		if (gui && properties.contains("font")) {
			UIManager.getLookAndFeelDefaults().put("defaultFont", new Font(properties.getProperty("font"), Font.PLAIN, 12));
			
		}
	}

	/**
	 * Quit the application.
	 */
	public void shutdown() {
		shutdownObservable.notify(this);
	}

	public String createUniqueId() {
		return UUID.randomUUID().toString().toUpperCase();
	}

	/**
	 * Return file name for a given resource name.
	 * 
	 * @param value
	 * @return
	 */
	public String findFile(String value) {
		// System.out.println(getProperty("inputPath"));
		if (value == null)
			return "";
		if (value.startsWith("/"))
			return value;
		if (value.contains(":")) {
			Element el = Element.fromString(value);
			return rootDataSource.getValue(el);
		}
		if (new File(getProperty("inputPath") + value).exists())
			return getProperty("inputPath") + value;
		if (new File(getProperty("outputPath") + value).exists())
			return getProperty("outputPath") + value;
		return value;
	}

	public Observable<Application> getUIModeObservable() {
		return modeObservable;
	}

	public Observable<Application> getShutdownObservable() {
		return shutdownObservable;
	}

	public UIMode getUIMode() {
		return mode;
	}

	public Collection<UIMode> getUIModes() {
		return modes;
	}

	public void setUIMode(UIMode m) {
		if (mode != m && modes.contains(m)) {
			mode = m;
			modeObservable.notify(this);
		}
	}

	public WindowSystem getWindowSystem() {
		return windowSystem;
	}

	protected static void addUIModes(Collection<UIMode> modes) {
		modes.add(new UIMode() {
			@Override
			public String getName() {
				return "Normal Mode";
			}

			@Override
			public boolean isLargeContent() {
				return false;
			}

			@Override
			public boolean isLargeUI() {
				return false;
			}
		});
		modes.add(new UIMode() {
			@Override
			public String getName() {
				return "Beamer Mode";
			}

			@Override
			public boolean isLargeContent() {
				return true;
			}

			@Override
			public boolean isLargeUI() {
				return false;
			}
		});
		modes.add(new UIMode() {
			@Override
			public String getName() {
				return "Tablet Mode";
			}

			@Override
			public boolean isLargeContent() {
				return true;
			}

			@Override
			public boolean isLargeUI() {
				return true;
			}
		});
	}
	
	/**
	 * Returns the repository of tool interface repositories. Modules should
	 * add or remove their own repositories here.
	 */
	public MetaRepository<Tool> getToolMetaRepository() {
		return toolRepository;
	}

	public RootDataSource getRootDataSource() {
		return rootDataSource;
	}

	public Observable<Message> getMessageObservable() {
		return messageObservable;
	}

	public void sendMessage(Message m) {
		messageObservable.notify(m);
	}

	public Set<Type> getTypes() {
		return types;
	}

	public String getProperty(String key) {
		if (!properties.containsKey(key)) {
			if (key.equals("inputPath"))
				setProperty(key, System.getProperty("user.home") + "/" + ".vanda/input/");
			if (key.equals("toolsPath"))
				setProperty(key, System.getProperty("user.home") + "/" + ".vanda/interfaces/");
			if (key.equals("lastInputPath"))
				setProperty(key, System.getProperty("user.home") + "/" + ".vanda/input/");
			if (key.equals("outputPath"))
				setProperty(key, System.getProperty("user.home") + "/" + ".vanda/output/");
		}
		return properties.getProperty(key);
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
		try {
			properties.storeToXML(new FileOutputStream(PROPERTIES_FILE), null);
		} catch (Exception e) {
			sendMessage(new ExceptionMessage(e));
		}
	}
	
	public void setModuleManager(ModuleManager moduleManager) {
		this.moduleManager = moduleManager;
	}
	
	public PreviewFactory getPreviewFactory(Type type) {
		PreviewFactory result = previewFactories.get(type);
		if (result == null)
			result = previewFactories.get(null);
		return result;
	}

	/**
	 * if type is null, pf will be regarded as fallback
	 * 
	 * @param type
	 * @param pf
	 */
	public void registerPreviewFactory(Type type, PreviewFactory pf) {
		previewFactories.put(type, pf);
	}
	
}
