package org.vanda.studio.modules.profile;

import java.util.ArrayList;
import java.util.WeakHashMap;

import org.vanda.studio.app.Generator;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.immutable.AtomicImmutableJob;
import org.vanda.studio.model.immutable.CompositeImmutableJob;
import org.vanda.studio.model.immutable.ImmutableJob;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.modules.profile.model.Fragment;
import org.vanda.studio.modules.profile.model.FragmentCompiler;
import org.vanda.studio.modules.profile.model.FragmentIO;
import org.vanda.studio.modules.profile.model.FragmentLinker;
import org.vanda.studio.modules.profile.model.Profiles;

public final class GeneratorImpl implements Generator {

	private FragmentIO io;
	private WeakHashMap<Object, Fragment> map;
	private Profiles prof;
	private FragmentLinker rootLinker;

	public GeneratorImpl(Profiles prof, FragmentIO io, FragmentLinker rootLinker) {
		this.prof = prof;
		this.io = io;
		this.rootLinker = rootLinker;
		map = new WeakHashMap<Object, Fragment>();
	}

	public Fragment generateFragment(ImmutableJob j) {
		Fragment result = map.get(j);
		if (result == null) {
			if (j instanceof AtomicImmutableJob) {
				AtomicImmutableJob aj = (AtomicImmutableJob) j;
				result = new Fragment(aj.getElement().getName());
			} else if (j instanceof CompositeImmutableJob) {
				CompositeImmutableJob cj = (CompositeImmutableJob) j;
				FragmentLinker fl = prof.getLinker(cj.getLinker().getId());
				assert (fl != null);
				Fragment inner = generateFragment(cj.getWorkflow());
				result = fl.link(inner, io);
			}
			assert (result != null);
			map.put(j, result);
		}
		return result;
	}

	public Fragment generateFragment(ImmutableWorkflow w) {
		Fragment result = map.get(w);
		if (result == null) {
			assert (w.getFragmentType() != null);
			FragmentCompiler fc = prof.getCompiler(w.getFragmentType());
			assert (fc != null);
			ArrayList<JobInfo> jobs = w.getChildren();
			ArrayList<Fragment> fragments = new ArrayList<Fragment>(jobs.size());
			for (int i = 0; i < jobs.size(); i++) {
				JobInfo ji = jobs.get(i);
				if (ji.job instanceof AtomicImmutableJob
						&& ((AtomicImmutableJob) ji.job).getElement() instanceof Tool
						|| ji.job instanceof CompositeImmutableJob) {
					fragments.add(generateFragment(jobs.get(i).job));
				} else
					fragments.add(null);
			}
			result = fc.compile(w.getName(), jobs, fragments);
			assert (result != null);
			map.put(w, result);
		}
		return result;
	}

	@Override
	public void generate(ImmutableWorkflow iwf) {
		Fragment root = generateFragment(iwf);
		rootLinker.link(root, io);
	}

}
