package org.vanda.studio.modules.previews;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
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
		app.registerPreviewFactory(new CompositeType("BerkeleyGrammar.sm6"),
				new BerkeleyGrammarPreviewFactory(app, ".prev"));
		app.registerPreviewFactory(new CompositeType("LAPCFG-Grammar"),
				new BerkeleyGrammarPreviewFactory(app, "/level1.grammar"));
		app.registerPreviewFactory(new CompositeType("EMSteps"),
				new DictionaryPreviewFactory(app));
		app.registerPreviewFactory(new CompositeType("Scores"),
				new ScoresPreviewFactory(app));
		app.registerPreviewFactory(new CompositeType("Alignments"),
				new AlignmentsPreviewFactory(app));
		app.registerPreviewFactory(null, new MonospacePreviewFactory());
		return null;
	}

}
