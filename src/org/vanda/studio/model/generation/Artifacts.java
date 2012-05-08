package org.vanda.studio.model.generation;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class containing some supporting functions for Artifacts and
 * ArtifactFactories.
 * 
 * @author mbue
 * 
 */
public class Artifacts {
	// TODO implement caching also for the other members
	private static SoftReference<ArrayList<Port>> _choiceInputs;
	private static SoftReference<List<Port>> choiceInputs;
	public static List<Port> identityInputs;
	public static List<Port> identityOutputs;
	public static List<Port> choiceOutputs;

	{
		identityInputs = Collections.singletonList(new Port("idin", "a"));
		identityOutputs = Collections.singletonList(new Port("idout", "a"));
		_choiceInputs = null;
		choiceInputs = null;
		choiceOutputs = Collections.singletonList(new Port("orout", "a"));
	}

	public static List<Port> getChoiceInputPorts(int n) {
		// retrieve (and if necessary create) the mutable list
		ArrayList<Port> _ci = null;
		if (_choiceInputs != null)
			_ci = _choiceInputs.get();
		if (_ci == null) {
			_ci = new ArrayList<Port>();
			_choiceInputs = new SoftReference<ArrayList<Port>>(_ci);
		}
		// retrieve (and if necessary create) the immutable view
		List<Port> ci = null;
		if (choiceInputs != null)
			ci = choiceInputs.get();
		if (ci == null) {
			ci = Collections.unmodifiableList(_ci);
			choiceInputs = new SoftReference<List<Port>>(ci);
		}
		// if necessary, add ports
		while (_ci.size() < n)
			_ci.add(new Port("orin#" + Integer.toString(_ci.size()), "a"));
		return ci.subList(0, n);
	}

	/**
	 * Default implementation of Artifact.getOutputs to be used for an identity
	 * artifact. This assumes that the identity operation is not represented in
	 * the fragment under generation; it merely passes on the input to the
	 * output as if nonextant.
	 * 
	 * @param inputs
	 * @return
	 */
	public static <T> List<T> getIdentityOutputs(List<T> inputs) {
		return inputs;
	}
}
