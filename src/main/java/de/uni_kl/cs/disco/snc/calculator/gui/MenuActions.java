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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import de.uni_kl.cs.disco.snc.calculator.SNC;
import de.uni_kl.cs.disco.snc.exceptions.FileOperationException;

/**
 * A static factory containing all menu actions.
 *
 * @author Sebastian Henningsen
 * @author Michael Beck
 */
public class MenuActions {

    static class LoadNetworkAction extends AbstractAction {

        public LoadNetworkAction(String name) {
            super(name);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            JFileChooser chooser = new JFileChooser();
            int opened = chooser.showOpenDialog(null);
            if (opened == JFileChooser.APPROVE_OPTION) {
                try {
                    SNC.getInstance().loadNetwork(chooser.getSelectedFile());
                } catch (FileOperationException e) {
                    System.out.println(e.getLine().equals("") ? "Error while loading network: " + e.getMessage() : "Error while loading in line: \"" + e.getLine() + "\". " + e.getMessage());
                }
            }
        }
    }

    static class SaveNetworkAction extends AbstractAction {

        public SaveNetworkAction(String name) {
            super(name);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            JFileChooser chooser = new JFileChooser();
            int saved = chooser.showSaveDialog(null);
            if (saved == JFileChooser.APPROVE_OPTION) {
                try {
                    SNC.getInstance().saveNetwork(chooser.getSelectedFile());
                } catch (FileOperationException e) {
                    System.out.println("Error while saving network: " + e.getMessage());

                }
            }
        }
    }

    static class ExitAction extends AbstractAction {

        MainWindow gui;

        public ExitAction(String name, MainWindow gui) {
            super(name);
            this.gui = gui;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            gui.close();
        }
    }

    static class UndoAction extends AbstractAction {

        public UndoAction(String name) {
            super(name);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            SNC.getInstance().undo();
            System.out.println("Undo");
        }
    }

    static class RedoAction extends AbstractAction {

        public RedoAction(String name) {
            super(name);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            SNC.getInstance().redo();
            System.out.println("Redo");
        }

    }

    static class AboutAction extends AbstractAction {

        public AboutAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            AboutDialog dialog = new AboutDialog();
            dialog.display();
        }
    }
}
