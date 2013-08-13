package org.vanda.util;

import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

@SuppressWarnings("serial")
public class JXRowHeaderTable extends JXTable {
	
	private Object[][] rh;
	private JXTable rht;
	
	public JXRowHeaderTable() {
		super();
		this.rht = new JXTable();
		rht.setBackground(rht.getTableHeader().getBackground());
		rht.setForeground(rht.getTableHeader().getForeground());
		rht.setSortable(false);
		rht.packAll();
	}
	
	public JXTable getRowHeader() {
		return rht;
	}
	
	public void setRowHeaderData(Object[] rh) {
		this.rh = new Object[rh.length][1];
		for (int i = 0; i < rh.length; i++)
			this.rh[i][0] = rh[i];
		((DefaultTableModel) rht.getModel()).setDataVector(this.rh, new String [] {""});
		rht.packAll();
	}
	
}
