package org.vanda.studio.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;

public final class DefaultPreviewFactory implements PreviewFactory {

	@SuppressWarnings("unused")
	private final Application app;

	public DefaultPreviewFactory(Application app) {
		this.app = app;
	}

	@Override
	public JComponent createPreview(String value) {
		JEditorPane editor = new JEditorPane();
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

	@Override
	public JComponent createSmallPreview(String absolutePath) {
		return createPreview(absolutePath);
	}

}
