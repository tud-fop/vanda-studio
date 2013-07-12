package org.vanda.fragment.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.execution.model.ExecutableWorkflow;
//import org.vanda.fragment.model.DataflowAnalysis;
import org.vanda.fragment.model.Fragment;
import org.vanda.fragment.model.FragmentBase;
import org.vanda.fragment.model.FragmentCompiler;
import org.vanda.fragment.model.FragmentIO;
import org.vanda.fragment.model.Generator;
import org.vanda.fragment.model.Profile;
import org.vanda.studio.app.Application;
import org.vanda.types.Type;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;

public class GeneratorImpl implements Generator, FragmentIO {

	private final Application app;
	private Profile prof;

//	static interface Functor<E extends Throwable> {
//		void run() throws E;
//	}

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
				public Fragment getFragment(String name) {
					return fragments.get(name);
				}
			};
		}

		public Fragment generateAtomic(Tool t) {
			Fragment result = map.get(t.getId());
			if (result == null) {
				result = prof.getFragmentToolMetaRepository().getRepository()
						.getItem(t.getId());
				assert (result != null);
				// TODO this ^^ should be guaranteed via tool interfaces
				map.put(t, result);
				fragments.put(result.getId(), result);
			}
			return result;
		}

//		public String generateFragment(DataflowAnalysis dfa) throws IOException {
		public String generateFragment(ExecutableWorkflow ewf) throws IOException {
//			MutableWorkflow w = dfa.getWorkflow();
//			Fragment result = map.get(w);
			Fragment result = map.get(ewf);
			if (result == null) {
//				String name = makeUnique(w.getName(), w);
				String name = makeUnique(ewf.getName(), ewf);
//				assert (dfa.getFragmentType() != null);
				assert (ewf.getFragmentType() != null);
//				FragmentCompiler fc = prof.getCompiler(dfa.getFragmentType());
				FragmentCompiler fc = prof.getCompiler(ewf.getFragmentType());
				assert (fc != null);
//				Job[] jobs = dfa.getSorted();
				Job[] jobs = ewf.getSortedJobs();
				final ArrayList<Fragment> frags = new ArrayList<Fragment>(
						jobs.length);
				for (final Job ji : jobs) {
					ji.visit(new ElementVisitor() {
						@Override
						public void visitLiteral(Literal l) {
							frags.add(null);
						}

						@Override
						public void visitTool(final Tool t) {
							frags.add(generateAtomic(t));
						}
					});
				}
//				result = fc.compile(name, dfa, frags, GeneratorImpl.this);
				result = fc.compile(name, ewf, frags, GeneratorImpl.this);
				assert (result != null);
//				map.put(w, result);
				map.put(ewf, result);
				this.fragments.put(result.getId(), result);
			}
			return result.getId();
		}

//		public Fragment generate(DataflowAnalysis dfa) throws IOException {
		public Fragment generate(ExecutableWorkflow ewf) throws IOException {
			String root = generateFragment(ewf);
//			String root = generateFragment(dfa);
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
//	public Fragment generate(DataflowAnalysis dfa) throws IOException {
	public Fragment generate(ExecutableWorkflow ewf) throws IOException {
		return new TheGenerator().generate(ewf);
//		return new TheGenerator().generate(dfa);
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
