package org.vanda.studio.modules.profile.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fragments represent small snippets of code. They are compositional, yet
 * for efficiency reasons (string manipulation) we only compose once we have
 * computed all the individual fragments. This is accomplished by maintaining a
 * list of "further" fragments. So in fact, fragments constitute a tree
 * structure. This structure is flattened using the compose method.
 * 
 * @author buechse
 *
 */
public class Fragment {
	public final String name;
	public final String auxiliary;
	public final List<Fragment> further;
	public final Set<String> imports;
	
	public Fragment(String name) {
		this.name = name;
		this.auxiliary = "";
		this.further = Collections.emptyList();
		this.imports = Collections.emptySet();
	}
	
	public Fragment(String name, String auxiliary, List<Fragment> further, Set<String> imports) {
		this.name = name;
		this.auxiliary = auxiliary;
		this.further = further;
		this.imports = imports;
	}
	
	public Fragment compose() {
		FragmentComposer fc = new FragmentComposer(name);
		fc.doIt(this);
		return fc.make();
	}
	
	private static final class FragmentComposer {
		private final String name;
		private final Set<String> im;
		private final StringBuilder sb;
		private final List<Fragment> fur;
		
		public FragmentComposer(String name) {
			this.name = name;
			im = new HashSet<String>();
			sb = new StringBuilder();
			fur = Collections.emptyList();
		}
		
		public Fragment make() {
			return new Fragment(name, sb.toString(), fur, im);
		}

		public void doIt(Fragment fragment) {
			im.addAll(fragment.imports);
			for (Fragment f : fragment.further)
				doIt(f);
			sb.append(fragment.auxiliary);
			sb.append('\n');
		}
	}
}
