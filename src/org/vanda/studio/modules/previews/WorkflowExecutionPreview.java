package org.vanda.studio.modules.previews;

import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.vanda.execution.model.ExecutableWorkflow;
import org.vanda.fragment.model.Fragment;
import org.vanda.fragment.model.Generator;
import org.vanda.presentationmodel.execution.PresentationModel;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.modules.workflows.run2.Run;
import org.vanda.types.Type;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Pair;
import org.vanda.view.View;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.TypeChecker;
import org.vanda.workflows.hyper.TypeCheckingException;
import org.vanda.workflows.serialization.Loader;

import com.mxgraph.swing.mxGraphComponent;

public class WorkflowExecutionPreview implements PreviewFactory {
	/**
	 * 
	 */
	public class WorkflowExecution {
		private final class CancelAction implements Action {
			private final Run run;

			private CancelAction(Run run) {
				this.run = run;
			}

			@Override
			public String getName() {
				return "Cancel";
			}

			@Override
			public void invoke() {
				run.cancel();
				app.getWindowSystem().disableAction(this);
			}
		}
		private final class RunAction implements Action {

			@Override
			public String getName() {
				return "Run";
			}

			@Override
			public void invoke() {
				Fragment frag = generate();
				if (frag != null) {
					System.out.println("invoked RunAction");
					Run run = new Run(app, getExecutableWorkflow(), frag);
					run.execute();
					app.getWindowSystem().addAction(
							component,
							new CancelAction(run),
							KeyStroke.getKeyStroke(KeyEvent.VK_C,
									KeyEvent.CTRL_MASK));
					app.getWindowSystem().disableAction(this);
				}
			}
		}
		JComponent component;

		ExecutableWorkflow ewf;

		PresentationModel pm;

		public WorkflowExecution(Pair<MutableWorkflow, Database> phd)
				throws TypeCheckingException {

			TypeChecker tc = new TypeChecker();
			phd.fst.typeCheck(tc);
			tc.check();
			Type fragmentType = tc.getFragmentType();
			Job[] sorted = null;

			try {
				sorted = phd.fst.getSorted();
			} catch (Exception e) {
				// FIXME send message that there are cycles
			}
			ewf = new ExecutableWorkflow(phd.fst, phd.snd, sorted, fragmentType);
			ewf.init();
			ewf.shift();
			View view = new View(ewf);
			pm = new PresentationModel(view, ewf);

			// setup component design
			mxGraphComponent component = (mxGraphComponent) pm
					.getVisualization().getGraphComponent();
			component.setDragEnabled(false);

			component.setPanning(true);
			component.getPageFormat().setOrientation(PageFormat.LANDSCAPE);
			component.setPageVisible(true);
			component
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			component
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

			component.setName(phd.fst.getName() + "Execution");
			this.component = component;
			app.getWindowSystem().addAction(component, new RunAction(),
					KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
		}

		private Fragment generate() {
			try {
				return prof.generate(getExecutableWorkflow());
			} catch (IOException e) {
				app.sendMessage(new ExceptionMessage(e));
			}
			return null;
		}

		public JComponent getComponent() {
			return component;
		}

		public ExecutableWorkflow getExecutableWorkflow() {
			return ewf;
		}

	}

	private final Application app;
	private final Generator prof;

	public WorkflowExecutionPreview(Application app, Generator prof) {
		this.app = app;
		this.prof = prof;
	}

	@Override
	public JComponent createPreview(String filePath) {
		Pair<MutableWorkflow, Database> phd;
		try {
			phd = new Loader(app.getToolMetaRepository().getRepository())
					.load(filePath);
			WorkflowExecution wfe = new WorkflowExecution(phd);
			return wfe.getComponent();
		} catch (Exception e) {
			app.sendMessage(new ExceptionMessage(e));
			return null;
		}
	}

	@Override
	public JComponent createSmallPreview(String absolutePath) {
		return createPreview(absolutePath);
	}

	@Override
	public void openEditor(String value) {
		// do nothing
	}

}
