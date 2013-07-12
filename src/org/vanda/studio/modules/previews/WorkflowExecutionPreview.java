package org.vanda.studio.modules.previews;

import java.awt.print.PageFormat;

import javax.swing.JComponent;
import javax.swing.ScrollPaneConstants;

import org.vanda.execution.model.ExecutableWorkflow;
import org.vanda.presentationmodel.execution.PresentationModel;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.types.Type;
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
		}

		public JComponent getComponent() {
			return component;
		}

	}

	private final Application app;

	public WorkflowExecutionPreview(Application app) {
		this.app = app;
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
