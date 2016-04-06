package org.vanda.studio.core;

import java.awt.Desktop;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.modules.previews.Previews;
import org.vanda.studio.modules.previews.Previews.Preview;
import org.vanda.util.Observer;

public class DefaultPreviewFactory implements PreviewFactory {
	private final List<WeakReference<Preview>> previews;
	private final Application app;
	private final String postfix;
	private Observer<Application> uiModeObserver;

	private interface FontSizeSelector {
		public Font setFontSize(Font f);
	}

	private static class NormalFontSelector implements FontSizeSelector {
		/**
		 * The size of the font if the beamer mode is disabled.
		 */
		private static final int normalFontSize;

		static {
			JLabel label = new JLabel("");
			normalFontSize = label.getFont().getSize();
		}

		@Override
		public Font setFontSize(Font f) {
			return new Font(f.getFontName(), f.getStyle(), normalFontSize);
		}

	}

	private static class LargeFontSelector implements FontSizeSelector {
		/**
		 * The size of the font if the beamer / tablet mode is enabled.
		 */
		private static final int largeFontSize = 25;

		@Override
		public Font setFontSize(Font f) {
			return new Font(f.getFontName(), f.getStyle(), largeFontSize);
		}

	}

	private class MonospacePreview extends JEditorPane implements Preview {
		private static final long serialVersionUID = 8616936262891458646L;
		private int update = 0;
		private FontSizeSelector fontSizeSelector;

		@Override
		public void beginUpdate() {
			update++;
		}

		@Override
		public void endUpdate() {
			update--;
			if (update == 0) {
				updateSizes();
			}
		}

		@Override
		public void setLargeContent(boolean large) {
			fontSizeSelector = large ? new LargeFontSelector() : new NormalFontSelector();
		}

		@Override
		public void setLargeUI(boolean mode) {
		}

		private void updateSizes() {
			setFont(fontSizeSelector.setFontSize(getFont()));
		}
		
		public MonospacePreview(String value) {
			setContentType("text/plain; charset=utf-8");
			setFont(new Font("Unifont", getFont().getStyle(), getFont().getSize()));
			setLargeContent(app.getUIMode().isLargeContent());
			updateSizes();
			Document doc = getDocument();
			try {
				// use buffering, reading one line at a time
				// FileReader always assumes default encoding is OK!
				BufferedReader input = new BufferedReader(new FileReader(value));
				try {
					String line = null; // not declared within while loop
					int i = 0;
					while (i < 10 && (line = input.readLine()) != null) {
						doc.insertString(doc.getLength(), line, null);
						doc.insertString(doc.getLength(), System.getProperty("line.separator"), null);
						i++;
					}
					if (input.readLine() != null) {
						doc.insertString(doc.getLength(), "[...]", null);
					}
				} finally {
					input.close();
				}
			} catch (BadLocationException ex) {
				// do nothing
			} catch (IOException ex) {
				// ex.printStackTrace();
				setText("unable to open file " + value);
			}
		}

	}

	public DefaultPreviewFactory(Application app) {
		this(app, "");
	}
		
	public DefaultPreviewFactory(Application app, String postfix) {
		this.app = app;
		this.postfix = postfix;
		this.previews = new ArrayList<WeakReference<Preview>>();
		uiModeObserver = new Previews.UIObserver(previews);
		app.getUIModeObservable().addObserver(uiModeObserver);
	}

	@Override
	public JComponent createPreview(String value) {
		MonospacePreview mp = new MonospacePreview(value + postfix);
		previews.add(new WeakReference<Preview>(mp));
		return new JScrollPane(mp);
	}

	@Override
	public void openEditor(final String value) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Desktop.getDesktop().open(new File(value));
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
