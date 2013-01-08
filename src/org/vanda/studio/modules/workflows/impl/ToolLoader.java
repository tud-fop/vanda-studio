package org.vanda.studio.modules.workflows.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.vanda.fragment.bash.RendererSelector;
import org.vanda.fragment.bash.RendererSelectors;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.common.Loader;
import org.vanda.types.Type;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Observer;
import org.vanda.util.TokenSource;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.ToolInterface;

public class ToolLoader implements Loader<ShellTool> {

	protected final Application app;
	protected final String path;
	protected final ToolInterface ti;

	public ToolLoader(Application app, String path, ToolInterface ti) {
		this.app = app;
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
		Set<String> imports = new HashSet<String>();
		imports.add(file.getAbsolutePath());
		Scanner sc = null;
		TokenSource ts = new TokenSource();
		Map<String, Type> tVars = new HashMap<String, Type>();
		try {
			sc = new Scanner(file);
			boolean nameFound = false;
			String id = "";
			String name = "";
			String description = "";
			String version = "";
			String category = "";
			String contact = "";
			RendererSelector rs = RendererSelectors.selectors[0];
			List<Port> inPorts = new ArrayList<Port>();
			List<Port> outPorts = new ArrayList<Port>();
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line.startsWith("#")) {
					String line1 = line.substring(1).trim();
					if (!nameFound) {
						name = line1;
						nameFound = true;
					} else if (line1.toLowerCase().startsWith("version"))
						version = line1.split(":")[1].trim();
					else if (line1.toLowerCase().startsWith("contact"))
						contact = line1.split(":")[1].trim();
					else if (line1.toLowerCase().startsWith("category"))
						category = line1.split(":")[1].trim();
					else if (line1.toLowerCase().startsWith("renderer")) {
						String renderer = line1.split(":")[1].trim();
						for (RendererSelector r : RendererSelectors.selectors)
							if (r.getIdentifier().equals(renderer)) {
								rs = r;
								break;
							}
					} else if (line1.toLowerCase().startsWith("in")) {
						String[] arr = line1.substring(2).trim().split("::");
						Type t = ShellTool.parseType(tVars, ts, arr[1].trim());
						inPorts.add(new Port(arr[0].trim(), t));
					} else if (line1.toLowerCase().startsWith("out")) {
						String[] arr = line1.substring(3).trim().split("::");
						Type t = ShellTool.parseType(tVars, ts, arr[1].trim());
						outPorts.add(new Port(arr[0].trim(), t));
					} else if (description != null && line1 == "")
						description = null;
					else
						description = line1;
				} else if (line.matches(".*\\(\\).*\\{")) {
					id = line.trim().split(" ")[0];
					if (!name.equals("")) {
						List<Port> in = new ArrayList<Port>();
						in.addAll(inPorts);
						inPorts.clear();
						List<Port> out = new ArrayList<Port>();
						out.addAll(outPorts);
						outPorts.clear();
						ShellTool t = new ShellTool(id, name, category, version,
								contact, description, in, out, imports, rs, ti);
						o.notify(t);
						nameFound = false;
						ts = new TokenSource();
						tVars.clear();
					}
				}
			}
		} catch (FileNotFoundException e) {
			app.sendMessage(new ExceptionMessage(e));
			// new ExceptionMessage(new Exception("Tool file "
			// + file.getAbsolutePath() + " can not be loaded.")));
		} finally {
			sc.close();
		}
	}

}
