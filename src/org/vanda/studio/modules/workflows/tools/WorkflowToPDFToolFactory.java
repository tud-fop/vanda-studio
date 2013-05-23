package org.vanda.studio.modules.workflows.tools;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.vanda.presentationmodel.PresentationModel;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.w3c.dom.Document;

import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

public final class WorkflowToPDFToolFactory implements ToolFactory {
	
	protected class ExportWorkflowToPDFAction implements Action {

		private final WorkflowEditor wfe;

		public ExportWorkflowToPDFAction(WorkflowEditor wfe) {
			this.wfe = wfe;
		}

		@Override
		public String getName() {
			return "Export to PDF...";
		}

		@Override
		public void invoke() {
			JFileChooser chooser = new JFileChooser("");
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setVisible(true);
			int result = chooser.showSaveDialog(null);

			if (result == JFileChooser.APPROVE_OPTION) {
				File chosenFile = chooser.getSelectedFile();
				try {
					PresentationModel pm = new PresentationModel(wfe.getView(), wfe);
					mxGraph graph = pm.getVisualization().getGraph();
					Document svg = mxCellRenderer.createSvgDocument(graph,
							null, 1, null, null);
					String code = mxUtils.getPrettyXml(svg
							.getDocumentElement());
					TranscoderInput input = new TranscoderInput(
							new StringReader(code));
					PDFTranscoder t = new PDFTranscoder();
					TranscoderOutput output = new TranscoderOutput(
							new FileOutputStream(chosenFile));
					t.transcode(input, output);
					output.getOutputStream().flush();
					output.getOutputStream().close();
				} catch (Exception e) {
					app.sendMessage(new ExceptionMessage(e));
				}
			}
		}
	}
	
	private Application app;
	
	public WorkflowToPDFToolFactory(Application app) {
		super();
		this.app = app;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		Action a = new ExportWorkflowToPDFAction(wfe);
		wfe.addAction(a, KeyStroke.getKeyStroke(KeyEvent.VK_P,
				KeyEvent.CTRL_MASK));
		return a;
	}
}