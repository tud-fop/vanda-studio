package org.vanda.fragment.bash;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.vanda.types.Type;
import org.vanda.util.Loader;
import org.vanda.util.Observer;
import org.vanda.util.TokenSource;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.ToolInterface;

public class ToolLoader implements Loader<ShellTool> {

	protected final String path;
	protected final ToolInterface ti;

	public ToolLoader(String path, ToolInterface ti) {
		this.path = path;
		this.ti = ti;
	}

	@Override
	public void load(Observer<ShellTool> o) {
		// TODO Auto-generated method stub
		for (File f : (new File(path)).listFiles()) {
			if (f.isFile() && f.getAbsolutePath().endsWith(".bash"))
				loadFromFile(f, o);
		}

	}

	public void loadFromFile(File file, Observer<ShellTool> o) {
		Loader loader = new Loader(ti, o);
		try {
			loader.init(file);
			loader.process();
		} catch (FileNotFoundException e) {
			System.err.println("Tool file " + file.getAbsolutePath()
					+ " can not be loaded.");
			// app.sendMessage(new ExceptionMessage(e));
			// new ExceptionMessage(new Exception("Tool file "
			// + file.getAbsolutePath() + " can not be loaded.")));
		} finally {
			loader.done();
		}
	}

	private static final class Builder {
		Set<String> imports;
		String id;
		String name;
		StringBuilder description;
		String version;
		String category;
		String contact;
		RendererSelector rs;
		List<Port> inPorts;
		List<Port> outPorts;
		ToolInterface ti;
		TokenSource ts;
		Map<String, Type> tVars;

		public Builder(ToolInterface ti) {
			this.ti = ti;
			reset();
		}

		public void reset() {
			id = "";
			name = "";
			description = new StringBuilder();
			version = "";
			category = "";
			contact = "";
			rs = RendererSelectors.selectors[0];
			inPorts = new ArrayList<Port>();
			outPorts = new ArrayList<Port>();
			ts = new TokenSource();
			tVars = new HashMap<String, Type>();
		}

		public ShellTool build() {
			return new ShellTool(id, name, category, version, contact,
					description.toString(), inPorts, outPorts, imports, rs, ti);

		}
	}

	private interface FieldProcessor {
		String getFieldName();

		void process(String line, Builder b);
	}

	private static class VersionProcessor implements FieldProcessor {

		@Override
		public String getFieldName() {
			return "version:";
		}

		@Override
		public void process(String line, Builder b) {
			b.version = line.substring(getFieldName().length()).trim();
		}
	}

	private static class ContactProcessor implements FieldProcessor {

		@Override
		public String getFieldName() {
			return "contact:";
		}

		@Override
		public void process(String line, Builder b) {
			b.contact = line.substring(getFieldName().length()).trim();
		}
	}

	private static class CategoryProcessor implements FieldProcessor {

		@Override
		public String getFieldName() {
			return "category:";
		}

		@Override
		public void process(String line, Builder b) {
			b.category = line.substring(getFieldName().length()).trim();
		}
	}

	private static class RendererProcessor implements FieldProcessor {

		@Override
		public String getFieldName() {
			return "renderer:";
		}

		@Override
		public void process(String line, Builder b) {
			String renderer = line.substring(getFieldName().length()).trim();
			for (RendererSelector r : RendererSelectors.selectors)
				if (r.getIdentifier().equals(renderer)) {
					b.rs = r;
					break;
				}
		}
	}

	private static class InPortProcessor implements FieldProcessor {

		@Override
		public String getFieldName() {
			return "in ";
		}

		@Override
		public void process(String line, Builder b) {
			String valtype = line.substring(getFieldName().length()).trim();
			String[] arr = valtype.split("::");
			Type t = ShellTool.parseType(b.tVars, b.ts, arr[1].trim());
			b.inPorts.add(new Port(arr[0].trim(), t));
		}
	}

	private static class OutPortProcessor implements FieldProcessor {

		@Override
		public String getFieldName() {
			return "out ";
		}

		@Override
		public void process(String line, Builder b) {
			String valtype = line.substring(getFieldName().length()).trim();
			String[] arr = valtype.split("::");
			Type t = ShellTool.parseType(b.tVars, b.ts, arr[1].trim());
			b.outPorts.add(new Port(arr[0].trim(), t));
		}
	}

	private static final class Loader {

		private interface ParserState {
			/**
			 * Returns true iff builder should try to build now
			 * @param line
			 * @return
			 */
			boolean handleLine(String line);

			void lookAhead(String line);
		}

		Builder b;
		Scanner sc;
		ParserState st;
		Observer<? super ShellTool> o;
		FieldProcessor[] fieldProcessors = { new VersionProcessor(),
				new ContactProcessor(), new CategoryProcessor(),
				new RendererProcessor(), new InPortProcessor(),
				new OutPortProcessor() };

		ParserState stHandleName = new ParserState() {
			@Override
			public boolean handleLine(String line) {
				if (line.startsWith("#")) {
					b.name = line.substring(1).trim();
					st = stHandleFields;
				}
				return false;
			}

			@Override
			public void lookAhead(String line) {
			}
		};

		ParserState stHandleFields = new ParserState() {
			@Override
			public boolean handleLine(String line) {
				line = line.substring(1).trim();
				if ("".equals(line))
					st = stHandleDescription;
				else
					for (FieldProcessor fp : fieldProcessors) {
						if (line.toLowerCase().startsWith(fp.getFieldName())) {
							fp.process(line, b);
							break; // only one field processor should match
						}
					}
				return false;
			}

			@Override
			public void lookAhead(String line) {
				if (!line.startsWith("#"))
					st = stHandleFunction;
			}
		};

		ParserState stHandleDescription = new ParserState() {
			@Override
			public boolean handleLine(String line) {
				b.description.append(line.substring(1).trim());
				return false;
			}

			@Override
			public void lookAhead(String line) {
				if (!line.startsWith("#"))
					st = stHandleFunction;
			}
		};

		ParserState stHandleFunction = new ParserState() {
			@Override
			public boolean handleLine(String line) {
				if (line.matches(".*\\(\\).*\\{")) {
					b.id = line.trim().split(" ")[0];
					return true;
				} else
					return false;
			}

			@Override
			public void lookAhead(String line) {
				if (line.startsWith("#"))
					st = stHandleName;
			}
		};

		public Loader(ToolInterface ti, Observer<? super ShellTool> o) {
			b = new Builder(ti);
			this.o = o;
		}

		public void init(File file) throws FileNotFoundException {
			b.imports = new HashSet<String>();
			b.imports.add(file.getAbsolutePath());
			st = stHandleName;
			sc = new Scanner(file);
		}

		public void process() {
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				st.lookAhead(line);
				if (st.handleLine(line)) {
					ShellTool t = b.build();
					o.notify(t);
					b.reset();
				}
			}
		}

		public void done() {
			if (sc != null) {
				sc.close();
				sc = null;
			}
		}

		@Override
		public void finalize() {
			done();
		}

	}
}