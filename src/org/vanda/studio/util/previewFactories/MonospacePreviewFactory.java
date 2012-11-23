package org.vanda.studio.util.previewFactories;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.vanda.studio.app.PreviewFactory;


public class MonospacePreviewFactory implements PreviewFactory {

	@Override
	public JComponent createPreview(String value) {
		JEditorPane editor = new JEditorPane();
		editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, editor.getFont().getSize()));
		JScrollPane result = new JScrollPane(editor);
		Document doc = editor.getDocument();
		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			BufferedReader input = new BufferedReader(new FileReader(value));
			try {
				String line = null; // not declared within while loop
				int i = 0;
				while (i < 10 && (line = input.readLine()) != null) {
					doc.insertString(doc.getLength(), line, null);
					doc.insertString(doc.getLength(),
							System.getProperty("line.separator"), null);
					i++;
				}
				if (input.readLine() != null){
					doc.insertString(doc.getLength(), "[...]", null);
				}
			} finally {
				input.close();
			}
		} catch (BadLocationException ex) {
			// do nothing
		} catch (IOException ex) {
			// ex.printStackTrace();
			editor.setText("unable to open file " + value);
		}
		return result;
	}

	@Override
	public void openEditor(final String value) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					System.out.println("xdg-open " + value);
					Runtime.getRuntime().exec("xdg-open " + value);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		t.start();
	}


}
