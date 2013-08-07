package org.vanda.studio.modules.previews;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
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
		JScrollPane result = new JScrollPane(editor);
		editor.setContentType("text/plain; charset=utf-8");
		editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, editor.getFont().getSize()));
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
				adoptFont(editor);
			}
		} catch (BadLocationException ex) {
			// do nothing
		} catch (IOException ex) {
			// ex.printStackTrace();
			editor.setText("unable to open file " + value);
		}
		return result;
	}

	/**
	 * Check, if all loaded symbols are can be displayed by the default font.
	 * If not, search among the system fonts for the font, that can display the
	 * longest prefix of the editor text. Set this font to the editor.
	 * @param editor
	 */
	private void adoptFont(JEditorPane editor) {
		String text = editor.getText();
		if (editor.getFont().canDisplayUpTo(text) != -1) {
			Font current = editor.getFont();
			for (Font f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
				if (f.canDisplayUpTo(text) == -1) {
					editor.setFont(new Font(f.getFontName(), Font.PLAIN, editor.getFont().getSize()));
					return;
				} 
				if (f.canDisplayUpTo(text) > current.canDisplayUpTo(text)) {
					current = f;
				}
			}
			editor.setFont(new Font(current.getFontName(), Font.PLAIN, editor.getFont().getSize()));
		}
	}

	@Override
	public void openEditor(final String value) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
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
	public JComponent createSmallPreview(String value) {
		return createPreview(value);
	}

}
