package org.vanda.studio.modules.previews;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.core.DefaultPreviewFactory;
import org.vanda.types.CompositeType;

public class PreviewsModule implements Module {

	@Override
	public String getName() {
		return "Preview Module";
	}

	@Override
	public Object createInstance(Application app) {
		app.registerPreviewFactory(new CompositeType("PennTreeCorpus"),
				new BerkeleyTreePreviewFactory(app));
		app.registerPreviewFactory(new CompositeType("BerkeleyGrammar"),
				new BerkeleyGrammarPreviewFactory(app, ".prev.grammar"));
		app.registerPreviewFactory(new CompositeType("VandaPCFG"),
				new VandaPCFGPreviewFactory());
		app.registerPreviewFactory(new CompositeType("TextualBerkeleyGrammar"),
				new BerkeleyGrammarPreviewFactory(app, ".grammar"));
		app.registerPreviewFactory(new CompositeType("LAPCFG-Grammar"),
				new BerkeleyGrammarPreviewFactory(app, "/level1.grammar"));
		app.registerPreviewFactory(new CompositeType("EMSteps"),
				new DictionaryPreviewFactory(app));
		app.registerPreviewFactory(new CompositeType("Scores"),
				new ScoresPreviewFactory(app));
		app.registerPreviewFactory(new CompositeType("Alignments"),
				new AlignmentsPreviewFactory(app));
		app.registerPreviewFactory(new CompositeType("PLCFRS"),
				new PLCFRSPreviewFactory(app, ".readable"));
		app.registerPreviewFactory(new CompositeType("NegraCorpus"),
				new NegraTreePreviewFactory(app, ".penn"));
		app.registerPreviewFactory(new CompositeType("log"),
				new LogPreviewFactory());
		app.registerPreviewFactory(new CompositeType("StPOStagger"),
				new DefaultPreviewFactory(app, ".props"));
		app.registerPreviewFactory(new CompositeType("Integer"),
				new PlainEchoPreviewFactory(app));
		app.registerPreviewFactory(new CompositeType("Double"),
				new PlainEchoPreviewFactory(app));
		app.registerPreviewFactory(null,
				new DefaultPreviewFactory(app));
		return null;
	}

}
