package org.vanda.fragment.bash;

import java.io.File;
import java.io.FileNotFoundException;

import org.vanda.fragment.bash.parser.ParserImpl;
import org.vanda.util.Loader;
import org.vanda.util.Observer;

public class ToolLoader implements Loader<ShellTool> {

	protected final String path;

	public ToolLoader(String path) {
		this.path = path;
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
		ParserImpl loader = new ParserImpl(o);
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
}