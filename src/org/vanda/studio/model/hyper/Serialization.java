package org.vanda.studio.model.hyper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.util.MultiplexObserver;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

// FIXME Integer instances where taken from Token should be interned!
// e.g., save as int, but upon loading, request the int from some static Token
// method

public class Serialization {
	
	public static MutableWorkflow<?> load(String pathToFile, Application app) {
		if (pathToFile == null)
			throw new NullPointerException("File path is set to " + pathToFile
					+ "!");

		File file = new File(pathToFile);
		XStream xs = new XStream();
		xs.registerConverter(new LinkerConverter(app));
		xs.registerConverter(new MultiplexObserverConverter(app));
		xs.registerConverter(new ToolConverter(app));
		Object result = null;
		try {
			result = xs.fromXML(file);
			
			// loading and deserialization was successful, check if file 
			// contains a valid HyperWorkflow
			if (result != null && result instanceof MutableWorkflow) {
				MutableWorkflow<?> root = (MutableWorkflow<?>) result;
				return root;
			}
			else
				return null;
		} catch (XStreamException xe) {
			throw new IllegalArgumentException(
					"The specified file does not contain a NestedHyperworkflow! - "
							+ pathToFile);
		}
	}
	
	public static boolean save(MutableWorkflow<?> hwf, String filename) {
		if (filename == null)
			throw new NullPointerException("File path is set to " + filename
					+ "!");

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(filename);
			
			if (fileWriter != null) {
				Writer output = new BufferedWriter(fileWriter);
				XStream xs = new XStream();
				xs.registerConverter(new LinkerConverter(null));
				xs.registerConverter(new MultiplexObserverConverter(null));
				xs.registerConverter(new ToolConverter(null));
				
				System.out.println("saving " + hwf);
				
				xs.toXML(hwf, output);
				return true;
			}
		} catch (IOException e) {
		}

		return false;
	}
	
	@SuppressWarnings("unchecked")
	private static class LinkerConverter implements Converter {

		Application app;
		
		public LinkerConverter(Application app) {
			this.app = app;
		}
		
		@Override
		public void marshal(Object value, HierarchicalStreamWriter writer,
				MarshallingContext context) {

			// simply save compiler id
			writer.startNode("linkerId");
			writer.setValue("TODO"); // TODO
			writer.endNode();	
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			
			// extract compiler id from xml and load it from repository
			reader.moveDown();
			String linkerId = reader.getValue();
			Linker<?,?> linker= app.getLinkerMetaRepository().getRepository().getItem(linkerId);
			reader.moveUp();
			
			return linker;
		}

		@Override
		public boolean canConvert(Class clazz) {
			// activate this converter for all classes that 
			// implement the Linker interface
			return Linker.class.isAssignableFrom(clazz);
		}

	}
	
	@SuppressWarnings("unchecked")
	private static class MultiplexObserverConverter implements Converter {
		Application app;
		
		public MultiplexObserverConverter(Application app) {
			this.app = app;
		}
		
		@Override
		public void marshal(Object value, HierarchicalStreamWriter writer,
				MarshallingContext context) {
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			
			// extract tool id from xml and load it from repository
			MultiplexObserver tool= new MultiplexObserver();			
			return tool;
		}

		@Override
		public boolean canConvert(Class clazz) {
			// activate this converter for all classes that 
			// extends MultiplexObserver
			return MultiplexObserver.class.isAssignableFrom(clazz);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static class ToolConverter implements Converter {
		Application app;
		
		public ToolConverter(Application app) {
			this.app = app;
		}
		
		@Override
		public void marshal(Object value, HierarchicalStreamWriter writer,
				MarshallingContext context) {

			// simply save tool id
			writer.startNode("toolId");
			writer.setValue(((Tool<?>)value).getId());
			writer.endNode();	
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			
			// extract tool id from xml and load it from repository
			reader.moveDown();
			String toolId = reader.getValue();
			Tool<?> tool= app.getToolMetaRepository().getRepository().getItem(toolId);
			reader.moveUp();
			
			return tool;
		}

		@Override
		public boolean canConvert(Class clazz) {
			// activate this converter for all classes that 
			// implement the Tool interface
			return Tool.class.isAssignableFrom(clazz);
		}
	}
}
