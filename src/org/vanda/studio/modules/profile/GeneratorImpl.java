package org.vanda.studio.modules.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.studio.app.Generator;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.immutable.AtomicImmutableJob;
import org.vanda.studio.model.immutable.CompositeImmutableJob;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.modules.profile.model.Fragment;
import org.vanda.studio.modules.profile.model.FragmentBase;
import org.vanda.studio.modules.profile.model.FragmentCompiler;
import org.vanda.studio.modules.profile.model.FragmentIO;
import org.vanda.studio.modules.profile.model.FragmentLinker;
import org.vanda.studio.modules.profile.model.Profiles;

public final class GeneratorImpl implements Generator {

	private FragmentIO io;
	private WeakHashMap<Object, Fragment> map;
	private HashMap<String, Fragment> fragments; // i.e., functions
	private Profiles prof;
	private FragmentBase fb;
	private FragmentLinker rootLinker;
	private final Map<String, Integer> uniqueMap;
	private final Map<Object, String> uniqueStrings;

	public GeneratorImpl(Profiles prof, FragmentIO io, FragmentLinker rootLinker) {
		this.prof = prof;
		this.io = io;
		this.rootLinker = rootLinker;
		uniqueMap = new HashMap<String, Integer>();
		uniqueStrings = new HashMap<Object, String>();
		map = new WeakHashMap<Object, Fragment>();
		fragments = new HashMap<String, Fragment>();
		fb = new FragmentBase() {
			@Override
			public Fragment getFragment(String name) {
				return fragments.get(name);
			}
		};
	}

	public String makeUnique(String prefix, Object key) {
		String result = uniqueStrings.get(key);
		if (result == null) {
			Integer n = uniqueMap.get(prefix);
			if (n == null)
				n = new Integer(0);
			uniqueMap.put(prefix, new Integer(n.intValue() + 1));
			result = prefix + "$" + n.toString();
			uniqueStrings.put(key, result);
		}
		return result;
	}

	public String generateAtomicFragment(AtomicImmutableJob j)
			throws IOException {
		assert (j.getElement() instanceof Tool);
		Fragment result = map.get(j.getElement().getId());
		if (result == null) {
			result = new Fragment(j.getElement().getId());
			map.put(j.getElement(), result);
			fragments.put(result.name, result);
		}
		return result.name;
	}

	public String generateCompositeFragment(CompositeImmutableJob j)
			throws IOException {
		Fragment result = map.get(j);
		if (result == null) {
			FragmentLinker fl = prof.getLinker(j.getLinker().getId());
			assert (fl != null);
			String inner = generateFragment(j.getWorkflow());
			result = fl.link(inner, fb, io);
			map.put(j, result);
			fragments.put(result.name, result);
		}
		return result.name;
	}

	public String generateFragment(ImmutableWorkflow w) throws IOException {
		Fragment result = map.get(w);
		if (result == null) {
			assert (w.getFragmentType() != null);
			FragmentCompiler fc = prof.getCompiler(w.getFragmentType());
			assert (fc != null);
			ArrayList<JobInfo> jobs = w.getChildren();
			ArrayList<String> fragments = new ArrayList<String>(jobs.size());
			for (int i = 0; i < jobs.size(); i++) {
				JobInfo ji = jobs.get(i);
				if (ji.job instanceof AtomicImmutableJob
						&& ((AtomicImmutableJob) ji.job).getElement() instanceof Tool)
					fragments
							.add(generateAtomicFragment((AtomicImmutableJob) jobs
									.get(i).job));
				else if (ji.job instanceof CompositeImmutableJob) {
					fragments
							.add(generateCompositeFragment((CompositeImmutableJob) jobs
									.get(i).job));
				} else
					fragments.add(null);
			}
			result = fc.compile(w.getName(), jobs, fragments);
			assert (result != null);
			map.put(w, result);
		}
		return result.name;
	}

	@Override
	public void generate(ImmutableWorkflow iwf) throws IOException {
		String root = generateFragment(iwf);
		rootLinker.link(root, fb, io);
	}

}
