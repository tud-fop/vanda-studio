package org.vanda.fragment.model;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.vanda.types.Type;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

// XXX removed: handle ports (see older versions)
public final class DataflowAnalysis {
	public static final String UNDEFINED = "UNDEFINED";

	private final MutableWorkflow workflow;
	private final Database db;
	private final Map<Location, String> values;
	private final Map<Tool, String> rootDirs;
	private final Job[] jobs;
	private final Type fragmentType;
	private boolean connected;

	public DataflowAnalysis(MutableWorkflow iwf, Database db, Job[] sorted, Type fragmentType) {
		workflow = iwf;
		this.db = db;
		values = new HashMap<Location, String>();
		rootDirs = new HashMap<Tool, String>();
		jobs = sorted;
		this.fragmentType = fragmentType;
		connected = false;
	}

	public Type getFragmentType() {
		return fragmentType;
	}

	public Job[] getSorted() {
		return jobs;
	}

	public void init() {
		Boolean cc = true;
		if (jobs == null)
			return;
		for (final Job ji : jobs) {
			if (ji.isConnected()) {
				ji.visit(new ElementVisitor() {
					@Override
					public void visitLiteral(Literal lit) {
						values.put(ji.bindings.get(ji.getOutputPorts().get(0)),
								db.get(lit.getKey()));
					}

					@Override
					public void visitTool(Tool t) {
						StringBuilder sb = new StringBuilder();
						sb.append('(');
						List<Port> ports = t.getInputPorts();
						if (ports.size() > 0)
							appendValue(sb, ji, ports.get(0));
						for (int i = 1; i < ports.size(); i++) {
							sb.append(',');
							appendValue(sb, ji, ports.get(i));
						}
						sb.append(')');
						String toolPrefix = Fragments.normalize(t.getId()) + "."
								+ md5sum(sb.toString());
						rootDirs.put(t, toolPrefix);
						for (Port op : t.getOutputPorts()) {
							String value = toolPrefix + "." + op.getIdentifier();
							values.put(ji.bindings.get(op), value);
						}

					}

				});
			} else {
				for (Port op : ji.getOutputPorts()) {
					Location variable = ji.bindings.get(op);
					StringBuilder sb = new StringBuilder();
					sb.append('[');
					sb.append(UNDEFINED);
					sb.append("] ");
					sb.append(variable.toString());
					values.put(variable, sb.toString());
				}
				cc = false;
			}
		}
		connected = cc;
	}

	private static String md5sum(String in) {
		try {
			byte[] bytesOfMessage = in.getBytes(Charset.forName("UTF-8"));
			MessageDigest md = MessageDigest.getInstance("MD5");
			return DatatypeConverter.printHexBinary(md.digest(bytesOfMessage));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("MD5 not supported by platform.");
			return null;
		}
	}

	private void appendValue(StringBuilder sb, Job j, Port p) {
		sb.append(p.getIdentifier());
		sb.append('=');
		sb.append(values.get(j.bindings.get(p)).replace('/', '#'));
	}

	public String getValue(Location address) {
		return values.get(address);
	}
	
	public String getRootDir(Tool t) {
		return rootDirs.get(t);
	}

	public MutableWorkflow getWorkflow() {
		return workflow;
	}
	
	public boolean isConnected() {
		return connected;
	}
}
