package org.vanda.fragment.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.fragment.model.DataflowAnalysis;
import org.vanda.fragment.model.Fragment;
import org.vanda.fragment.model.FragmentBase;
import org.vanda.fragment.model.FragmentCompiler;
import org.vanda.fragment.model.FragmentIO;
import org.vanda.fragment.model.Generator;
import org.vanda.fragment.model.Profile;
import org.vanda.studio.app.Application;
import org.vanda.types.Type;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.immutable.AtomicImmutableJob;
import org.vanda.workflows.immutable.ImmutableWorkflow;
import org.vanda.workflows.immutable.JobInfo;

public class GeneratorImpl implements Generator, FragmentIO {

	private final Application app;
	private Profile prof;

	// The Generator class encapsulates stuff that should not be kept around
	// all the time, and resource acquisition is initialization, blah blah
	protected class TheGenerator {

		// private final FragmentIO io;
		private final WeakHashMap<Object, Fragment> map;
		private final HashMap<String, Fragment> fragments; // i.e., functions
		private final FragmentBase fb;
		private final Map<String, Integer> uniqueMap;
		private final Map<Object, String> uniqueStrings;

		public TheGenerator() {
			// io = ProfileImpl.this;
			uniqueMap = new HashMap<String, Integer>();
			uniqueStrings = new HashMap<Object, String>();
			map = new WeakHashMap<Object, Fragment>();
			fragments = new HashMap<String, Fragment>();
			fb = new FragmentBase() {
				@Override
				public Tool getConversionTool(Type from, Type to) {
					return null;
					/*
					 * return LinkerUtil.getConversionTool(ProfileImpl.this.app
					 * .getConverterToolMetaRepository().getRepository(), from,
					 * to);
					 */
				}

				@Override
				public Fragment getFragment(String name) {
					return fragments.get(name);
				}
			};
		}

		public String generateAtomic(AtomicImmutableJob j) throws IOException {
			assert (j.getElement() instanceof Tool);
			Fragment result = map.get(j.getElement().getId());
			if (result == null) {
				result = new Fragment(j.getElement().getId());
				map.put(j.getElement(), result);
				fragments.put(result.name, result);
			}
			return result.name;
		}

		public String generateFragment(DataflowAnalysis dfa) throws IOException {
			ImmutableWorkflow w = dfa.getWorkflow();
			Fragment result = map.get(w);
			if (result == null) {
				String name = makeUnique(w.getName(), w);
				assert (w.getFragmentType() != null);
				FragmentCompiler fc = prof.getCompiler(w.getFragmentType());
				assert (fc != null);
				ArrayList<JobInfo> jobs = w.getChildren();
				ArrayList<String> frags = new ArrayList<String>(jobs.size());
				for (int i = 0; i < jobs.size(); i++) {
					JobInfo ji = jobs.get(i);
					if (ji.job instanceof AtomicImmutableJob
							&& ((AtomicImmutableJob) ji.job).getElement() instanceof Tool)
						frags.add(generateAtomic((AtomicImmutableJob) ji.job));
					else
						frags.add(null);
				}
				result = fc.compile(name, dfa, frags, GeneratorImpl.this);
				assert (result != null);
				map.put(w, result);
				this.fragments.put(result.name, result);
			}
			return result.name;
		}

		public Fragment generate(DataflowAnalysis dfa) throws IOException {
			String root = generateFragment(dfa);
			return prof.getRootLinker(getRootType()).link(root, null, null,
					null, null, fb, GeneratorImpl.this);
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

	}

	@Override
	public Fragment generate(DataflowAnalysis dfa) throws IOException {
		return new TheGenerator().generate(dfa);
	}

	public GeneratorImpl(Application app, Profile prof) {
		this.app = app;
		this.prof = prof;
	}

	@Override
	public File createFile(String name) throws IOException {
		File result = new File(app.getProperty("outputPath") + name);
		result.createNewFile();
		return result;
	}

	@Override
	public String findFile(String value) {
		return app.findFile(value);
	}

	@Override
	public Type getRootType() {
		return prof.getRootType();
	}

}
