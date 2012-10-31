package org.vanda.studio.modules.profile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Profile;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.immutable.AtomicImmutableJob;
import org.vanda.studio.model.immutable.CompositeImmutableJob;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.modules.common.LinkerUtil;
import org.vanda.studio.modules.profile.concrete.RootLinker;
import org.vanda.studio.modules.profile.model.Fragment;
import org.vanda.studio.modules.profile.model.FragmentBase;
import org.vanda.studio.modules.profile.model.FragmentCompiler;
import org.vanda.studio.modules.profile.model.FragmentIO;
import org.vanda.studio.modules.profile.model.FragmentLinker;
import org.vanda.studio.modules.profile.model.Profiles;
import org.vanda.studio.util.TokenSource.Token;

public class ProfileImpl implements Profile, FragmentIO {

//	private static final String basePath = "/home/mbue/workspace/vanda/experiment/";
	private static final String basePath = System.getProperty("user.home") + "/"
			+ ".vanda/output/";
	private final Application app;
	private Profiles prof;
	private FragmentLinker rootLinker;

	

	private final FragmentIO io;
	private final WeakHashMap<Object, Fragment> map;
	private final HashMap<String, Fragment> fragments; // i.e., functions
	private final FragmentBase fb;
	private final Map<String, Integer> uniqueMap;
	private final Map<Object, String> uniqueStrings;

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

	public String generateComposite(ImmutableWorkflow parent, JobInfo ji,
			CompositeImmutableJob j) throws IOException {
		Fragment result = map.get(j);
		if (result == null) {
			FragmentLinker fl = prof.getLinker(j.getLinker().getId());
			assert (fl != null);
			ImmutableWorkflow w = j.getWorkflow();
			String inner = generateFragment(w);
			List<Type> outerinput = new ArrayList<Type>();
			List<Type> innerinput = new ArrayList<Type>();
			List<Type> inneroutput = new ArrayList<Type>();
			List<Type> outeroutput = new ArrayList<Type>();
			for (Token var : ji.inputs)
				outerinput.add(parent.getType(var));
			for (Token var : ji.outputs)
				outeroutput.add(parent.getType(var));
			for (Token var : w.getInputPortVariables())
				innerinput.add(w.getType(var));
			for (Token var : w.getOutputPortVariables())
				inneroutput.add(w.getType(var));
			result = fl.link(inner, outerinput, innerinput, inneroutput,
					outeroutput, fb, io);
			map.put(j, result);
			fragments.put(result.name, result);
		}
		return result.name;
	}

	public String generateFragment(ImmutableWorkflow w) throws IOException {
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
				else if (ji.job instanceof CompositeImmutableJob)
					frags.add(generateComposite(w, ji,
							(CompositeImmutableJob) ji.job));
				else
					frags.add(null);
			}
			result = fc.compile(name, w, frags);
			assert (result != null);
			map.put(w, result);
			this.fragments.put(result.name, result);
		}
		return result.name;
	}

	@Override
	public void generate(ImmutableWorkflow iwf) throws IOException {
		String root = generateFragment(iwf);
		rootLinker.link(root, null, null, null, null, fb, io);
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
	
	
	
	
	
	public ProfileImpl(Application app, Profiles prof) {
		this.app = app;
		this.prof = prof;
		rootLinker = new RootLinker();
		this.io = this;
		uniqueMap = new HashMap<String, Integer>();
		uniqueStrings = new HashMap<Object, String>();
		map = new WeakHashMap<Object, Fragment>();
		fragments = new HashMap<String, Fragment>();
		fb = new FragmentBase() {
			@Override
			public Tool getConversionTool(Type from, Type to) {
				return LinkerUtil.getConversionTool(ProfileImpl.this.app
						.getConverterToolMetaRepository().getRepository(),
						from, to);
			}

			@Override
			public Fragment getFragment(String name) {
				return fragments.get(name);
			}
		};
	}

	@Override
	public File createFile(String name) throws IOException {
		System.out.println("Creating file.");
		File result = new File(basePath + name);
		System.out.println(result.getAbsolutePath());
		result.createNewFile();
		return result;
	}

	@Override
	public String getCategory() {
		return "profiles";
	}

	@Override
	public String getContact() {
		return "Matthias.Buechse@tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return "Generates code using simple compositional fragments";
	}

	@Override
	public String getId() {
		return "fragment-profile";
	}

	@Override
	public String getName() {
		return "Fragment Profile";
	}

	@Override
	public Type getRootType() {
		return Types.shellType;
	}

	@Override
	public String getVersion() {
		return "0.1";
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
		
	}

}
