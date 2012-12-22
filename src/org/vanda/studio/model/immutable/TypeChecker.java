package org.vanda.studio.model.immutable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.types.Equation;
import org.vanda.studio.model.types.MergeFunction;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.TypeVariable;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource;
import org.vanda.studio.util.TokenSource.Token;

// XXX removed: handle linker (see older versions)
public final class TypeChecker {

	// use this to store target port information
	// implements equals and hashCode to make it usable for sets
	public static class EqInfo {
		public final Token address;
		public final int port;

		public EqInfo(Token address, int port) {
			this.address = address;
			this.port = port;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof EqInfo) {
				// address is assumed to be interned, so compare by reference
				return ((EqInfo) other).address == address
						&& ((EqInfo) other).port == port;
			} else
				return false;
		}

		@Override
		public int hashCode() {
			return address.hashCode() + port;
		}
	}

	public static class EqInfoMerge implements MergeFunction<Set<EqInfo>> {

		@Override
		public Set<EqInfo> merge(Set<EqInfo> x1, Set<EqInfo> x2) {
			if (x1 == null)
				return x2;
			else if (x2 == null)
				return x1;
			else {
				Set<EqInfo> result = new HashSet<EqInfo>();
				result.addAll(x1);
				result.addAll(x2);
				return result;
			}
		}

	}

	private final List<Equation<Set<EqInfo>>> eqs;
	private final TokenSource variableSource;
	private final TokenSource freshSource;
	private final Token fragmentTypeToken;
	private final TypeVariable fragmentTypeVariable;
	private Type[] types;
	private Type fragmentType;

	public TypeChecker(TokenSource variableSource)
			throws CloneNotSupportedException {
		eqs = new LinkedList<Equation<Set<EqInfo>>>();
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
		eqs.add(new Equation<Set<EqInfo>>(fragmentTypeVariable, ft
				.rename(rename), null));
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
					Equation<Set<EqInfo>> eq = new Equation<Set<EqInfo>>(
							new TypeVariable(ji.inputs.get(i)), in.get(i)
									.getType().rename(rename),
							Collections.singleton(new EqInfo(ji.address, i)));
					eqs.add(eq);
				}
			}
			for (int i = 0; i < ou.size(); i++) {
				Equation<Set<EqInfo>> eq = new Equation<Set<EqInfo>>(
						new TypeVariable(ji.outputs.get(i)), ou.get(i)
								.getType().rename(rename), null);
				eqs.add(eq);
			}
		}
	}

	public void check() throws Exception {
		List<Pair<String, Set<EqInfo>>> errors = Types.unify(eqs,
				new EqInfoMerge());
		if (errors.isEmpty()) {
			types = new Type[variableSource.getMaxToken()];
			for (Equation<?> eq : eqs) {
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
		} else {
			throw new TypeCheckingException(errors);
		}
	}

	public Type getFragmentType() {
		return fragmentType;
	}

	public Type[] getTypes() {
		return types;
	}
}