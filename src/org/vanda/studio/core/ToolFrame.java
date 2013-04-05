package org.vanda.studio.core;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

import org.vanda.studio.app.LayoutAssortment;
import org.vanda.studio.app.LayoutSelector;

@SuppressWarnings("serial")
class ToolFrame extends JInternalFrame implements LayoutSelector {
	private final LayoutSelector layout;
	
	public ToolFrame(JComponent c, LayoutSelector layout) {
		super(c.getName(), true);
		this.layout = layout;
		add(c);
		pack();
		setVisible(true);
		setIconifiable(true);
	}

	@Override
	public <L> L selectLayout(LayoutAssortment<L> la) {
		return layout.selectLayout(la);
	}
}