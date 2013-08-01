package org.vanda.workflows.data;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.JobVisitor;
import org.vanda.workflows.hyper.Jobs;
import org.vanda.workflows.hyper.Location;

// XXX removed: handle ports (see older versions)
public final class DataflowAnalysis implements JobVisitor {
	public static final String UNDEFINED = "UNDEFINED";
	public static final String ROW_ID = "rowID";

	private Map<Integer, String> assignment_;
	private boolean connected;
	private Map<Job, String> jobIds;   // distinguish jobs that perform the same operation, need not persist
	private Map<Job, String> jobSpecs; // identify jobs that perform the same operation, persist
	private Map<Location, String> values;

	public DataflowAnalysis() {
	}

	public void init(Map<Integer, String> assignment, Job[] sorted) {
		assignment_ = assignment;
		connected = true;
		jobIds = new HashMap<Job, String>();
		jobSpecs = new HashMap<Job, String>();
		values = new HashMap<Location, String>();
		if (sorted == null)
			return;
		Jobs.visitAll(sorted, this);
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
	
	private String computeJobId(Job j, Tool t) {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(Integer.toHexString(j.hashCode()));
		sb.append(',');
		String rid = assignment_.get(ROW_ID);
		if (rid == null)
			rid = Integer.toHexString(assignment_.hashCode());
		sb.append(rid);
		sb.append(')');
		return md5sum(sb.toString());
	}

	private String computeJobSpec(Job j, Tool t) {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		List<Port> ports = t.getInputPorts();
		if (ports.size() > 0)
			appendValue(sb, j, ports.get(0));
		for (int i = 1; i < ports.size(); i++) {
			sb.append(',');
			appendValue(sb, j, ports.get(i));
		}
		sb.append(')');
		return normalize(t.getId()) + "." + md5sum(sb.toString());
	}

	public String getValue(Location address) {
		return values.get(address);
	}

	public String getJobId(Job j) {
		return jobIds.get(j);
	}

	public String getJobSpec(Job j) {
		return jobSpecs.get(j);
	}

	public boolean isConnected() {
		return connected;
	}

	public static String normalize(String name) {
		return name.replace('$', '_').replace(' ', '_');
	}

	@Override
	public void visitLiteral(Job j, Literal l) {
		values.put(j.bindings.get(j.getOutputPorts().get(0)), assignment_.get(l.getKey()));
		System.out.println(l + " " + assignment_.get(l.getKey()));
	}

	@Override
	public void visitTool(Job j, Tool t) {
		if (j.isConnected()) {
			String jobSpec = computeJobSpec(j, t);
			jobSpecs.put(j, jobSpec);
			jobIds.put(j, computeJobId(j, t));
			for (Port op : t.getOutputPorts()) {
				String value = jobSpec + "." + op.getIdentifier();
				values.put(j.bindings.get(op), value);
			}
		} else {
			for (Port op : j.getOutputPorts()) {
				Location variable = j.bindings.get(op);
				StringBuilder sb = new StringBuilder();
				sb.append('[');
				sb.append(UNDEFINED);
				sb.append("] ");
				sb.append(variable.toString());
				values.put(variable, sb.toString());
			}
			connected = false;
		}
	}
}
