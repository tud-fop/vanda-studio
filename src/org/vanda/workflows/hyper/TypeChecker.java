package org.vanda.workflows.hyper;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vanda.types.Equation;
import org.vanda.types.MergeFunction;
import org.vanda.types.Type;
import org.vanda.types.TypeVariable;
import org.vanda.types.Types;
import org.vanda.util.Pair;
import org.vanda.workflows.elements.Port;

// XXX removed: handle linker (see older versions)
public final class TypeChecker {

	public static class EqInfoMerge implements
			MergeFunction<Set<ConnectionKey>> {

		@Override
		public Set<ConnectionKey> merge(Set<ConnectionKey> x1,
				Set<ConnectionKey> x2) {
			if (x1 == null)
				return x2;
			else if (x2 == null)
				return x1;
			else {
				Set<ConnectionKey> result = new HashSet<ConnectionKey>();
				result.addAll(x1);
				result.addAll(x2);
				return result;
			}
		}

	}

	private final List<Equation<Set<ConnectionKey>>> eqs;
	private final TypeVariable fragmentTypeVariable;
	private final Set<Object> variables;
	private final Map<Object, Type> types;
	private Type fragmentType;

	public TypeChecker() throws CloneNotSupportedException {
		eqs = new LinkedList<Equation<Set<ConnectionKey>>>();
		variables = new HashSet<Object>();
		fragmentTypeVariable = new TypeVariable(new Object());
		types = new HashMap<Object, Type>();
		fragmentType = null;
	}

	public void addFragmentTypeEquation(Type ft) {
		Map<Object, Object> rename = new HashMap<Object, Object>();
		ft.freshMap(rename);
		eqs.add(new Equation<Set<ConnectionKey>>(fragmentTypeVariable, ft
				.rename(rename), null));
	}

	public void addDataFlowEquations(Job ji) {
		Map<Object, Object> rename = new HashMap<Object, Object>();
		List<Port> in = ji.getInputPorts();
		List<Port> ou = ji.getOutputPorts();
		for (Port p : in)
			p.getType().freshMap(rename);
		for (Port p : ou)
			p.getType().freshMap(rename);
		for (Port ip : in) {
			Object variable = ji.bindings.get(ip);
			if (variable != null)
				eqs.add(new Equation<Set<ConnectionKey>>(new TypeVariable(
						variable), ip.getType().rename(rename), Collections
						.singleton(new ConnectionKey(ji, ip))));
		}
		for (Port op : ou) {
			Object variable = ji.bindings.get(op);
			variables.add(variable);
			eqs.add(new Equation<Set<ConnectionKey>>(new TypeVariable(
					variable), op.getType().rename(rename), null));
		}
	}

	public void check() throws Exception {
		List<Pair<String, Set<ConnectionKey>>> errors = Types.unify(eqs,
				new EqInfoMerge());
		if (errors.isEmpty()) {
			for (Equation<?> eq : eqs) {
				Object variable = ((TypeVariable) eq.lhs).variable;
				if (variables.contains(variable))
					types.put(variable, eq.rhs);
				if (variable == fragmentTypeVariable.variable)
					fragmentType = eq.rhs;
			}
			for (Object variable : variables)
				if (!types.containsKey(variable))
					types.put(variable, new TypeVariable(variable));
			if (fragmentType == null)
				fragmentType = fragmentTypeVariable;
		} else {
			throw new TypeCheckingException(errors);
		}
	}

	public Type getFragmentType() {
		return fragmentType;
	}

	public Map<Object, Type> getTypes() {
		return types;
	}
}