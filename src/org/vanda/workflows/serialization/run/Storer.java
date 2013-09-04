package org.vanda.workflows.serialization.run;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.vanda.studio.modules.workflows.run2.RunConfig;

import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

public class Storer {
	public void store(RunConfig rc, String filename) throws IOException {
		Writer writer = new FileWriter(new File(filename));
		final PrettyPrintWriter ppw = new PrettyPrintWriter(writer);
		ppw.startNode("runconfig");
		ppw.addAttribute("path", rc.getPath());
		ppw.startNode("priorities");
		for (String id : rc.getJobPriorities().keySet()) {
			ppw.startNode("job");
			ppw.addAttribute("id", id);
			ppw.addAttribute("priority", Integer.toHexString(rc.getJobPriorities().get(id)));
			ppw.endNode();
		}
		ppw.endNode(); // priorities
		ppw.endNode(); // runconfig

	}
}
