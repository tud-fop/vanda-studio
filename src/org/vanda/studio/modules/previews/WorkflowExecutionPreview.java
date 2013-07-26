package org.vanda.studio.modules.previews;

import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import org.vanda.execution.model.ExecutableWorkflow;
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
				// TODO retrieve "id" from some .run file, no need to build
				// fragment again
				String id = generate();
				if (id != null) {
					System.out.println("invoked RunAction");
					Run run = new Run(app, getExecutableWorkflow().getObserver(), id);
					run.run();
					app.getWindowSystem().addAction(component, new CancelAction(run),
							KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
					app.getWindowSystem().disableAction(this);
				}
			}
		}

		JComponent component;

		ExecutableWorkflow ewf;

		PresentationModel pm;

		public WorkflowExecution(Pair<MutableWorkflow, Database> phd) throws TypeCheckingException {

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
			mxGraphComponent gc = (mxGraphComponent) pm.getVisualization().getGraphComponent();
			gc.setDragEnabled(false);

			gc.setPanning(true);
			gc.getPageFormat().setOrientation(PageFormat.LANDSCAPE);
			gc.setPageVisible(true);
			gc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			gc.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

			gc.setName(phd.fst.getName() + "Execution");
			component = gc;
			app.getWindowSystem().addAction(gc, new RunAction(),
					KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
		}

		private String generate() {
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
			phd = new Loader(app.getToolMetaRepository().getRepository()).load(filePath);
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
