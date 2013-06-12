package org.vanda.util;

import org.jdesktop.swingx.JXTable;

@SuppressWarnings("serial")
public class JXRowHeaderTable extends JXTable {
	
	private Object[][] rh;
	
	public JXTable getRowHeader() {
		JXTable jl = new JXTable(rh, new String[] {""});
		jl.packAll();
		jl.setBackground(jl.getTableHeader().getBackground());
		jl.setForeground(jl.getTableHeader().getForeground());
		jl.setBorder(jl.getTableHeader().getBorder());
		jl.setSortable(false);
		return jl;
	}
	
	public void setRowHeaderData(Object[] rh) {
		this.rh = new Object[rh.length][1];
		for (int i = 0; i < rh.length; i++)
			this.rh[i][0] = rh[i];
	}

}
