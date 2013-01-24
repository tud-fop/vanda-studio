package org.vanda.studio.modules.workflows.jgraph;

import com.mxgraph.swing.util.mxGraphTransferable;

@SuppressWarnings("serial")
public class XmxGraphTransferable extends mxGraphTransferable {

	private String data;

	public XmxGraphTransferable(mxGraphTransferable t, String data) {
		super(t.getCells(), t.getBounds());
		this.data = data;
	}

	@Override
	protected String getPlainData() {
		return data;
	}

}