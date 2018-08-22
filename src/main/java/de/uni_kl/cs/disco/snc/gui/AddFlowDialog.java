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

package de.uni_kl.cs.disco.snc.gui;

import java.awt.GridLayout;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.uni_kl.cs.disco.snc.SNC;
import de.uni_kl.cs.disco.snc.commands.AddFlowCommand;
import de.uni_kl.cs.disco.snc.commands.Command;
import de.uni_kl.cs.disco.snc.symbolic_math.Arrival;
import de.uni_kl.cs.disco.snc.symbolic_math.functions.ConstantFunction;

/**
 * WARNING: Do not use this class!
 * 
 * @author Sebastian Henningsen
 */
public class AddFlowDialog {
    private JPanel panel;
    private JLabel alias;
    private JLabel service;
    private JTextField aliasField;
    private GridLayout layout;

    public AddFlowDialog() {
        panel = new JPanel();
        
        alias = new JLabel("Alias of the flow: ");
        service = new JLabel("Arrival Type: ");
        aliasField = new JTextField();
        
        panel.add(alias);
        panel.add(aliasField);
        panel.add(service);
        
        layout = new GridLayout(0, 1);
        panel.setLayout(layout);
    }

    public void display() {
        int result = JOptionPane.showConfirmDialog(null, panel, "Add Flow", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            SNC snc = SNC.getInstance();
            List<Integer> dummyList = new ArrayList<Integer>();
            dummyList.add(1);
            Command cmd = new AddFlowCommand(aliasField.getText(), new Arrival(new ConstantFunction(0), new ConstantFunction(1), snc.getCurrentNetwork()), dummyList, dummyList, -1, snc);
            snc.invokeCommand(cmd);
            
            // Just for debugging
            System.out.println(aliasField.getText());
        }
    }
}
