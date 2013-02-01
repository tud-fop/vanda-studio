package org.vanda.workflows.hyper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Repository;
import org.vanda.util.TokenSource;
import org.vanda.workflows.elements.Tool;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

@SuppressWarnings("rawtypes")
public final class Serialization {

	private final XStream xs;

	public Serialization(Repository<Tool> tr) {
		// app
		// .getSemanticsModuleMetaRepository().getRepository()
		// .getItem("profile").getToolMetaRepository().getRepository()
		xs = createXStream(tr);
	}

	private XStream createXStream(Repository<Tool> tr) {
		XStream result = new XStream();
		result.registerConverter(new InternedIntegerConverter());
		result.registerConverter(new MultiplexObserverConverter());
		result.registerConverter(new ToolConverter(tr));
		result.addImmutableType(TokenSource.Token.class);
		result.addImmutableType(Tool.class);
		result.alias("ovsm.hyper.AtomicJob", Job.class);
		result.aliasPackage("ovsu", "org.vanda.util");
		result.alias("org.vanda.studio.modules.profile.ShellTool",
				Tool.class);
		result.aliasPackage("ovsm.types", "org.vanda.types");
		result.aliasPackage("ovsm", "org.vanda.workflows");
		return result;
	}

	public MutableWorkflow load(String pathToFile) throws Exception {
		try {
			MutableWorkflow result = (MutableWorkflow) xs.fromXML(new File(
					pathToFile));
			result.rebind();
			return result;
		} catch (Exception e) {
			throw new Exception("An error occurred loading " + pathToFile, e);
		}
	}

	public void save(MutableWorkflow hwf, String filename) throws Exception {
		try {
			FileWriter fileWriter = new FileWriter(filename);

			if (fileWriter != null) {
				Writer output = new BufferedWriter(fileWriter);
				xs.toXML(hwf, output);
			}
		} catch (Exception e) {
			throw new Exception("An error occurred saving " + filename, e);
		}
	}

	private static class InternedIntegerConverter implements
			SingleValueConverter {

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

	private static class MultiplexObserverConverter implements Converter {
		@Override
		public void marshal(Object value, HierarchicalStreamWriter writer,
				MarshallingContext context) {
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			return new MultiplexObserver();
		}

		@Override
		public boolean canConvert(Class clazz) {
			return MultiplexObserver.class.isAssignableFrom(clazz);
		}
	}

	private static class ToolConverter implements SingleValueConverter {
		Repository<Tool> tr;

		public ToolConverter(Repository<Tool> tr) {
			this.tr = tr;
		}

		@Override
		public boolean canConvert(Class clazz) {
			// activate this converter for all classes that
			// implement the Tool interface
			return Tool.class.isAssignableFrom(clazz);
		}

		@Override
		public Object fromString(String str) {
			return tr.getItem(str);
		}

		@Override
		public String toString(Object obj) {
			Tool t = (Tool) obj;
			return t.getId();
		}
	}

}
