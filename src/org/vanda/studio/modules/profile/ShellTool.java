package org.vanda.studio.modules.profile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RendererAssortment;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.types.CompositeType;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.TypeVariable;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.TokenSource;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringStack;

public class ShellTool extends Tool {

	private String id;
	private String name;
	private String category;
	private String version;
	private String contact;
	private String description;
	private List<Port> inPorts;
	private List<Port> outPorts;
	private Set<String> imports;

	public ShellTool(String id, String name, String category, String version,
			String contact, String description, List<Port> inPorts,
			List<Port> outPorts, Set<String> imports) {
		super();
		this.id = id;
		this.name = name;
		this.category = category;
		this.version = version;
		this.contact = contact;
		this.description = description;
		this.inPorts = inPorts;
		this.outPorts = outPorts;
		this.imports = new HashSet<String>(imports);
	}

	@Override
	public Type getFragmentType() {
		return Types.shellType;
	}

	@Override
	public List<Port> getInputPorts() {
		return inPorts;
	}

	@Override
	public List<Port> getOutputPorts() {
		return outPorts;
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectAlgorithmRenderer();
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public String getContact() {
		return contact;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public void appendActions(List<Action> as) {
	}

	@Override
	public Set<String> getImports() {
		return imports;
	}

	public static Type parseType(Map<String, Type> m, TokenSource ts, String s1) {
		Stack<String> st = new StringStack();
		int idx = 0;
		String t = "";
		while (idx < s1.length()) {
			if ("()".contains(s1.substring(idx, idx + 1))) {
				if (!t.equals(""))
					st.add(0, String.copyValueOf(t.trim().toCharArray()));
				st.add(0, s1.substring(idx, idx + 1));
				t = "";
			} else if (s1.substring(idx, idx + 1).equals(",")) {
				st.add(0, String.copyValueOf(t.trim().toCharArray()));
				t = "";
			} else {
				t += s1.substring(idx, idx + 1);
			}
			idx++;
		}
		if (!t.equals(""))
			st.add(0, t.trim());
		return parseType(m, ts, st);
	}

	public static Type parseType(Map<String, Type> m, TokenSource ts, Stack<String> st) {
		String s = st.pop();
		Type t;
		List<Type> subTypes = new ArrayList<Type>();
		if (!st.empty() && st.peek().equals("(")) {
			st.pop();
			while (!st.peek().equals(")")) {
				Type t1 = parseType(m, ts, st);
				subTypes.add(t1);
			}
			st.pop();
			t = new CompositeType(s, subTypes);
		} else {
			if (Character.isLowerCase(s.charAt(0))) {
				if (!m.containsKey(s)) {
					t = new TypeVariable(ts.makeToken());
					m.put(s, t);
				} else {
					t = m.get(s);
				}
			} else {
				t = new CompositeType(s);
			}
		}
		return t;
	}
}
