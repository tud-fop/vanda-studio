package org.vanda.studio.modules.workflows;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.ToolFactory;
import org.vanda.studio.app.WorkflowEditor;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.modules.workflows.jgraph.DrecksAdapter;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.ExceptionMessage;
import org.w3c.dom.Document;

import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

final class WorkflowToPDFToolFactory implements ToolFactory {
	
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
					DrecksAdapter da = new DrecksAdapter(wfe.getModel());
					mxGraph graph = da.getGraph();
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
	public String getCategory() {
		return "Export";
	}

	@Override
	public String getContact() {
		return "Matthias.Buechse@tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return "Permits exporting the workflow to PDF";
	}

	@Override
	public String getId() {
		return "export-to-pdf";
	}

	@Override
	public String getName() {
		return "PDF export";
	}

	@Override
	public String getVersion() {
		return "2012-12-14";
	}

	@Override
	public void visit(RepositoryItemVisitor v) {

	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		Action a = new ExportWorkflowToPDFAction(wfe);
		wfe.addAction(a, KeyStroke.getKeyStroke(KeyEvent.VK_P,
				KeyEvent.CTRL_MASK));
		return a;
	}
}