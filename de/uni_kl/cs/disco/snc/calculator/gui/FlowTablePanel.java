/*
 *  (c) 2017 Michael A. Beck, Sebastian Henningsen
 *  		disco | Distributed Computer Systems Lab
 *  		University of Kaiserslautern, Germany
 *  All Rights Reserved.
 *
 * This software is work in progress and is released in the hope that it will
 * be useful to the scientific community. It is provided "as is" without
 * express or implied warranty, including but not limited to the correctness
 * of the code or its suitability for any particular purpose.
 *
 * This software is provided under the MIT License, however, we would 
 * appreciate it if you contacted the respective authors prior to commercial use.
 *
 * If you find our software useful, we would appreciate if you mentioned it
 * in any publication arising from the use of this software or acknowledge
 * our work otherwise. We would also like to hear of any fixes or useful
 */
package de.uni_kl.cs.disco.snc.calculator.gui;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import de.uni_kl.cs.disco.snc.calculator.SNC;
import de.uni_kl.cs.disco.snc.calculator.network.Flow;
import de.uni_kl.cs.disco.snc.calculator.network.NetworkListener;
import de.uni_kl.cs.disco.snc.calculator.network.Vertex;

/**
 * This class enables the GUI to display a table of all {@link Flow} and their attributes of
 * a given {@link Network}. Note that at the moment, only one network is possible.
 * It uses a {@link NetworkListener} to keep track of changes.
 * @author Sebastian Henningsen
 * @author Michael Beck
 */
public class FlowTablePanel {

    private final JScrollPane scrollPane;
    private final JTable table;
    private final DefaultTableModel tableModel;

    FlowTablePanel() {
        String[] colNames = {"ID", "Name", "Arrival", "Route", "Priorities"};
        tableModel = new DefaultTableModel(colNames, 0);
        table = new JTable(tableModel);
        scrollPane = new JScrollPane(table);
        SNC.getInstance().registerNetworkListener(new NetworkChangeListener());
    }

    /**
     * Returns the JPanel on which everything is displayed.
     * @return
     */
    public JScrollPane getPanel() {
        return scrollPane;
    }

    private class NetworkChangeListener implements NetworkListener {

        @Override
        public void vertexAdded(Vertex newVertex) {
            // Not of concern for us
        }

        @Override
        public void vertexRemoved(Vertex removedVertex) {
            // Not of concern for us, will be handled by flowChanged
        }

        @Override
        public void flowAdded(Flow newFlow) {
            Object[] data = {newFlow.getID(), newFlow.getAlias(), newFlow.getInitialArrival(), newFlow.getVerticeIDs().toString(), newFlow.getPriorities().toString()};
            tableModel.addRow(data);
        }

        @Override
        public void flowRemoved(Flow removedFlow) {
            int id;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                id = (Integer) tableModel.getValueAt(i, 0);
                if (id == removedFlow.getID()) {
                    tableModel.removeRow(i);
                    break;
                }
            }
        }

        @Override
        public void flowChanged(Flow changedFlow) {
            Object[] data = {changedFlow.getID(), changedFlow.getAlias(), changedFlow.getInitialArrival(), changedFlow.getVerticeIDs().toString(), changedFlow.getPriorities().toString()};

            int id;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                id = (Integer) tableModel.getValueAt(i, 0);
                if (id == changedFlow.getID()) {
                    tableModel.removeRow(i);
                    tableModel.insertRow(i, data);
                    break;
                }
            }
        }

        @Override
        public void vertexChanged(Vertex changedVertex) {
            // Only of concern for us if the priorities changed
        }
        
        @Override
        public void clear() {
            tableModel.setRowCount(0);
        }

    }
}
