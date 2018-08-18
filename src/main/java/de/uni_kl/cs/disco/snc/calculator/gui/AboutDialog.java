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

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * This dialog displays a short about message showing the current version 
 * and a link to the project homepage.
 * 
 * @author Sebastian Henningsen
 */
public class AboutDialog {
    private final JPanel panel;
    private final JTextArea about;
    private final String text;
    private final String version;
    
    /**
     * Constructs an about dialog. The displayed text is stored directly in the class.
     */
    public AboutDialog() {
        version = "2.1pre";
        text = "DISCO Stochastic Network Calculator version " 
        		+ version 
        		+ ".\n"
        		+ "For more information visit our project homepage http://disco.cs.uni-kl.de/index.php/projects/disco-snc";
        panel = new JPanel();
        
        about = new JTextArea(text);
        about.setEditable(false);
        panel.add(about);
    }
    
    /**
     * Displays the dialog
     */
    public void display() {
        JOptionPane.showMessageDialog(null, about, "About DISCO SNC", JOptionPane.INFORMATION_MESSAGE);
    }
}
