package org.vanda.studio.modules.workflows;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.KeyStroke;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Profile;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.ExceptionMessage;

public class RunTool implements ToolFactory {

	private static final class Tool {
		// private final WorkflowEditor wfe;
		private final Model m;
		private final Application app;
		private Profile prof;

		public Tool(WorkflowEditor wfe, Model m) {
			// this.wfe = wfe;
			this.m = m;
			app = wfe.getApplication();
			prof = app.getProfileMetaRepository().getRepository()
					.getItem("fragment-profile");
			if (prof != null)
				wfe.addAction(new GenerateAction(), KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK));
		}

		private final class GenerateAction implements Action {

			@Override
			public String getName() {
				return "Generate";
			}

			@Override
			public void invoke() {
				List<ImmutableWorkflow> unfolded = m.getUnfolded();
				if (unfolded != null
						&& unfolded.size() != 0
						&& Types.canUnify(unfolded.get(0).getFragmentType(),
								prof.getRootType())) {
					ImmutableWorkflow root = new ImmutableWorkflow(m.getRoot()
							.getName(), unfolded);
					try {
						prof.createGenerator().generate(root);
					} catch (IOException e) {
						app.sendMessage(new ExceptionMessage(e));
					}
				}
			}

		}

	}

	@Override
	public Object instantiate(WorkflowEditor wfe, Model m) {
		return new Tool(wfe, m);
	}

}
