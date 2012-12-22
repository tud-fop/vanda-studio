package org.vanda.studio.model.immutable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.types.Equation;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.TypeVariable;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource;
import org.vanda.studio.util.TokenSource.Token;

// XXX removed: handle linker (see older versions)
final class TypeChecker {
	private final Map<Equation, Pair<Token, Integer>> eqs;
	private final TokenSource variableSource;
	private final TokenSource freshSource;
	private final Token fragmentTypeToken;
	private final TypeVariable fragmentTypeVariable;
	private Type[] types;
	private Type fragmentType;

	public TypeChecker(TokenSource variableSource)
			throws CloneNotSupportedException {
		eqs = new HashMap<Equation, Pair<Token, Integer>>();
		this.variableSource = variableSource;
		freshSource = variableSource.clone();
		fragmentTypeToken = freshSource.makeToken();
		fragmentTypeVariable = new TypeVariable(fragmentTypeToken);
		types = null;
		fragmentType = null;
	}

	public void addFragmentTypeEquation(Type ft) {
		Map<Token, Token> rename = new HashMap<Token, Token>();
		ft.freshMap(freshSource, rename);
		eqs.put(new Equation(fragmentTypeVariable, ft.rename(rename)), null);
	}

	public void addDataFlowEquations(JobInfo ji) {
		Map<Token, Token> rename = new HashMap<Token, Token>();
		List<Port> in = ji.job.getInputPorts();
		List<Port> ou = ji.job.getOutputPorts();
		if (in != null && ou != null) {
			assert (in.size() == ji.inputs.size() && ou.size() == ji.outputs
					.size());
			for (Port p : in)
				p.getType().freshMap(freshSource, rename);
			for (Port p : ou)
				p.getType().freshMap(freshSource, rename);
			for (int i = 0; i < in.size(); i++) {
				if (ji.inputs.get(i) != null) {
					Equation eq = new Equation(new TypeVariable(
							ji.inputs.get(i)), in.get(i).getType()
							.rename(rename));
					eqs.put(eq, new Pair<Token, Integer>(ji.address, i));
				}
			}
			for (int i = 0; i < ou.size(); i++) {
				Equation eq = new Equation(new TypeVariable(ji.outputs.get(i)),
						ou.get(i).getType().rename(rename));
				eqs.put(eq, new Pair<Token, Integer>(ji.address, i));
			}
		}
	}

	public void check() throws Exception {
		Types.unify(eqs);
		types = new Type[variableSource.getMaxToken()];
		for (Equation eq : eqs.keySet()) {
			Token i = ((TypeVariable) eq.lhs).variable;
			if (i.intValue() < types.length)
				types[i.intValue()] = eq.rhs;
			if (i.intValue() == fragmentTypeToken.intValue())
				fragmentType = eq.rhs;
		}
		for (int i = 0; i < types.length; i++)
			if (types[i] == null)
				types[i] = new TypeVariable(TokenSource.getToken(i));
		if (fragmentType == null)
			fragmentType = fragmentTypeVariable;
	}

	public Type getFragmentType() {
		return fragmentType;
	}

	public Type[] getTypes() {
		return types;
	}
}