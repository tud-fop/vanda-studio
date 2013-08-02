package org.vanda.workflows.serialization;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.vanda.workflows.data.Database;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

public class Storer {

	public void store(MutableWorkflow w, Database d, String filename) throws Exception {
		Writer writer = new FileWriter(new File(filename));
		final PrettyPrintWriter ppw = new PrettyPrintWriter(writer);
		ppw.startNode("workflow");
		ppw.addAttribute("name", w.getName());
		for (Job j : w.getChildren()) {
			ppw.startNode("job");
			j.visit(new ElementVisitor() {
				@Override
				public void visitLiteral(Literal l) {
					ppw.startNode("literal");
					ppw.addAttribute("type", l.getType().toString());
					// ppw.addAttribute("value", l.getName());
					ppw.addAttribute("name", l.getName());
					ppw.addAttribute("key", l.getKey()); // Integer.toHexString(l.getKey()));
					ppw.endNode(); // literal
				}

				@Override
				public void visitTool(Tool t) {
					ppw.startNode("tool");
					ppw.addAttribute("id", t.getId());
					ppw.endNode(); // tool
				}
			});
			for (Map.Entry<Port, Location> e : j.bindings.entrySet()) {
				ppw.startNode("bind");
				ppw.addAttribute("port", e.getKey().getIdentifier());
				ppw.addAttribute("variable", Integer.toHexString(e.getValue().hashCode()));
				ppw.endNode(); // bind
			}
			ppw.startNode("geometry");
			ppw.addAttribute("x", Double.toString(j.getX()));
			ppw.addAttribute("y", Double.toString(j.getY()));
			ppw.addAttribute("width", Double.toString(j.getWidth()));
			ppw.addAttribute("height", Double.toString(j.getHeight()));
			ppw.endNode(); // geometry
			if (j.getId() != null) {
				ppw.startNode("id");
				ppw.addAttribute("id", j.getId());
				ppw.endNode();
			}
			ppw.endNode(); // job
		}
		ppw.startNode("database");
		for (int i = 0; i < d.getSize(); i++) {
			HashMap<String, String> row = d.getRow(i);
			ppw.startNode("row");
			for (Map.Entry<String, String> e : row.entrySet()) {
				ppw.startNode("assignment");
				ppw.addAttribute("key", e.getKey()); // Integer.toHexString(e.getKey().intValue()));
				ppw.addAttribute("value", e.getValue());
				ppw.endNode(); // assignment
			}
			ppw.endNode(); // row
		}
		ppw.endNode(); // database
		ppw.endNode(); // workflow
	}

}
