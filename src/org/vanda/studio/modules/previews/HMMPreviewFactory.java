package org.vanda.studio.modules.previews;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.Box;
import java.awt.Dimension;

import com.mxgraph.view.mxGraph;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.util.mxConstants;
import com.mxgraph.layout.mxParallelEdgeLayout;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.util.ExceptionMessage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HMMPreviewFactory implements PreviewFactory {

	private Application app;
	
	public HMMPreviewFactory(Application app) {
		super();
		this.app = app;
	}
	
	public class HMMPreview extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JButton bText;
		private JButton bModel;
		private JComponent cText;
		private JComponent cModel;
		private File source;
		private static final String MODEL = "show model";
		private static final String TEXT = "show text";

		public HMMPreview(String path) {
			setLayout(new BorderLayout());
			
			source = new File(path);
			
			bText = new JButton(new AbstractAction(TEXT) {

				@Override
				public void actionPerformed(ActionEvent e) {
					showText();
				}
			});
			
			bModel = new JButton(new AbstractAction(MODEL) {

				@Override
				public void actionPerformed(ActionEvent e) {
					showModel();
				}
			});
			
			JPanel buttons = new JPanel();
			buttons.add(bModel);
			buttons.add(bText);
			add(buttons, BorderLayout.PAGE_END);

			buildModel();
			buildText();
			
			add(cModel, BorderLayout.CENTER);
			showText();
		}
		
		private void showText() {
			remove(cModel);
			bText.setVisible(false);
			bModel.setVisible(true);
			
			add(cText, BorderLayout.CENTER);
			
			revalidate();
			repaint();

		}
		
		private void showModel() {
			remove(cText);
			bText.setVisible(true);
			bModel.setVisible(false);
			
			add(cModel, BorderLayout.CENTER);
			
			revalidate();
			repaint();
		}
			
		private void buildText() {
			JEditorPane editorPane = new JEditorPane();
			editorPane.setEditable(false);

		    try {
		        editorPane.setPage(source.toURI().toURL());
		    } catch (IOException e) {
		        System.err.println("Attempted to read a bad URL: ");
		    }
		    
		    cText = new JScrollPane(editorPane);
		}
		
		private void buildModel() {

			int nbStates = 0;
			int count = 0;
			List<Double> pi = new ArrayList<Double>();
			List<List<Double>> a = new ArrayList<List<Double>>();
			List<String> b = new ArrayList<String>();
			//read hmm
			try(Scanner readHmm = new Scanner(source)) {
				int state = 0;

				while (readHmm.hasNextLine()) {
					String line = readHmm.nextLine();
					switch(state) {
					  	case 0:
							if(line.startsWith("Hmm")) {
								state = 1;
							}
							break;
						case 1:
							if(line.startsWith("NbStates")) {
								nbStates = Integer.parseInt(line.substring(8).trim());
								state = 2;
							}
							break;
						case 2:
							if(line.startsWith("State")) {
								state = 3;
							}
							break;
						case 3:
							if(line.startsWith("Pi")) {
								pi.add(Double.parseDouble(line.substring(3).trim()));
								state = 4;
							}
							break;
						case 4:
							if(line.startsWith("A")) {
								List<Double> a2 = new ArrayList<Double>();
								for(String entry: line.substring(2).trim().split(" ")) {
									a2.add(Double.parseDouble(entry));
								}
								a.add(a2);
								state = 5;
							}
							break;
						case 5:
							if(line.startsWith("IntegerOPDF")) {
								b.add(line.substring(12).trim());
								state = 2;
								count += 1;
							}
							break;
						default:
							//nothing
					}
				}
				
			}catch (FileNotFoundException e){
				app.sendMessage(new ExceptionMessage(e));
			}
			
			if(nbStates == 0 || nbStates != count) {
				return;
			}
			
			mxGraph jGraph = new mxGraph();
			jGraph.getModel().beginUpdate();
			try {
				Object[] vi = new Object[nbStates];
				for(int i = 0; i < nbStates; i++) {
					vi[i] = jGraph.insertVertex(jGraph.getDefaultParent(), null, String.format("S_%s", i), 20 + 200*(i%2), 40+200*(i/2), 80, 30 ,mxConstants.STYLE_SHAPE+"="+ mxConstants.SHAPE_ELLIPSE);
					Object x = jGraph.insertVertex(jGraph.getDefaultParent(), null, "", 60 + 200*(i%2), 200*(i/2), 0, 0, mxConstants.STYLE_OPACITY+"=0");
					jGraph.insertEdge(jGraph.getDefaultParent(), null,String.format("%s", pi.get(i)), x , vi[i]);
				}
				for(int i = 0; i < nbStates; i++) {
					for(int j = 0; j < nbStates; j++) {
						jGraph.insertEdge(jGraph.getDefaultParent(), null,String.format("%s", a.get(i).get(j)),  vi[i], vi[j]);
					}
					
				}
				new mxParallelEdgeLayout(jGraph).execute(jGraph.getDefaultParent());
				
			}finally {
				jGraph.getModel().endUpdate();
			}
			
			mxGraphComponent comp = new mxGraphComponent(jGraph);
			comp.setConnectable(false);
			
			cModel = comp;
		}
	}
	
	@Override
	public JComponent createPreview(String path) {
		return new HMMPreview(path);
	}

	@Override
	public JComponent createSmallPreview(String absolutePath) {
		return createPreview(absolutePath);
	}

	@Override
	public void openEditor(final String value) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Desktop.getDesktop().open(new File(value));
				} catch (IOException e) {
					app.sendMessage(new ExceptionMessage(e));
				}
			}
		});

		t.start();
	}

}
