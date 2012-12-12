package org.vanda.studio.modules.profile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.modules.common.Loader;
import org.vanda.studio.util.ExceptionMessage;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.TokenSource;

public class ToolLoader implements Loader<Tool> {
	
	protected final Application app;
	protected final String path;
	
	public ToolLoader(Application app, String path) {
		this.app = app;
		this.path = path;
	}

	@Override
	public void load(Observer<Tool> o) {
		// TODO Auto-generated method stub
		for (File f : (new File(path)).listFiles()) {
			if (f.isFile() && f.getAbsolutePath().endsWith(".bash"))
				loadFromFile(f, o);
		}

	}


	public void loadFromFile(File file, Observer<Tool> o) {
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
					else if (line1.toLowerCase().startsWith("in")) {
						String[] arr = line1.substring(2).trim()
								.split("::");
						Type t = ShellTool.parseType(tVars, ts, arr[1].trim());
						inPorts.add(new Port(arr[0].trim(), t));
					} else if (line1.toLowerCase().startsWith("out")) {
						String[] arr = line1.substring(3).trim()
								.split("::");
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
						Tool t = new ShellTool(new String(id), new String(
								name), new String(category), new String(
								version), new String(contact), new String(
								description), in, out, imports);
						o.notify(t);
						nameFound = false;
						ts = new TokenSource();
						tVars.clear();
					}
				}
			}
		} catch (FileNotFoundException e) {
			app.sendMessage(new ExceptionMessage(e));
			//new ExceptionMessage(new Exception("Tool file "
			//		+ file.getAbsolutePath() + " can not be loaded.")));
		} finally {
			sc.close();
		}
	}

}
