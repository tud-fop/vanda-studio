package org.vanda.studio.model.hyper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.TokenSource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

@SuppressWarnings("rawtypes")
public final class Serialization {

	private static XStream xs = null;

	private static XStream getXStream(Application app) {
		if (xs == null) {
			xs = new XStream();
			xs.registerConverter(new InternedIntegerConverter());
			xs.registerConverter(new LinkerConverter(app));
			xs.registerConverter(new MultiplexObserverConverter());
			xs.registerConverter(new ToolConverter(app));
			xs.addImmutableType(TokenSource.Token.class);
			xs.aliasPackage("ovsu", "org.vanda.studio.util");
			xs.aliasPackage("ovsm", "org.vanda.studio.model");
		}
		return xs;
	}

	public static MutableWorkflow<?> load(Application app, String pathToFile)
			throws Exception {
		try {
			MutableWorkflow<?> result = (MutableWorkflow<?>) getXStream(app)
					.fromXML(new File(pathToFile));
			return result;
		} catch (Exception e) {
			throw new Exception("An error occurred loading " + pathToFile, e);
		}
	}

	public static void save(Application app, MutableWorkflow<?> hwf,
			String filename) throws Exception {
		try {
			FileWriter fileWriter = new FileWriter(filename);

			if (fileWriter != null) {
				Writer output = new BufferedWriter(fileWriter);
				getXStream(app).toXML(hwf, output);
			}
		} catch (Exception e) {
			throw new Exception("An error occurred saving " + filename, e);
		}
	}
	
	private static class InternedIntegerConverter implements SingleValueConverter {

		public InternedIntegerConverter() {
		}

		@Override
		public boolean canConvert(Class clazz) {
			// activate this converter for all classes that
			// implement the Linker interface
			return TokenSource.Token.class.isAssignableFrom(clazz);
		}

		@Override
		public Object fromString(String str) {
			return TokenSource.getToken(Integer.parseInt(str));
		}

		@Override
		public String toString(Object obj) {
			return obj.toString();
		}

	}

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
			Linker<?, ?> linker = app.getLinkerMetaRepository().getRepository()
					.getItem(linkerId);
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

	private static class MultiplexObserverConverter implements Converter {
		@Override
		public void marshal(Object value, HierarchicalStreamWriter writer,
				MarshallingContext context) {
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {

			// extract tool id from xml and load it from repository
			MultiplexObserver tool = new MultiplexObserver();
			return tool;
		}

		@Override
		public boolean canConvert(Class clazz) {
			// activate this converter for all classes that
			// extends MultiplexObserver
			return MultiplexObserver.class.isAssignableFrom(clazz);
		}
	}

	private static class ToolConverter implements SingleValueConverter {
		Application app;

		public ToolConverter(Application app) {
			this.app = app;
		}

		@Override
		public boolean canConvert(Class clazz) {
			// activate this converter for all classes that
			// implement the Tool interface
			return Tool.class.isAssignableFrom(clazz);
		}

		@Override
		public Object fromString(String str) {
			return app.getToolMetaRepository().getRepository().getItem(str);
		}

		@Override
		public String toString(Object obj) {
			return ((Tool<?>) obj).getId();
		}
	}
	
	
	// DJobInfo
}
