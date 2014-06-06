/*
 *  (c) 2013 Michael A. Beck, disco | Distributed Computer Systems Lab
 *                                  University of Kaiserslautern, Germany
 *         All Rights Reserved.
 *
 *  This software is work in progress and is released in the hope that it will
 *  be useful to the scientific community. It is provided "as is" without
 *  express or implied warranty, including but not limited to the correctness
 *  of the code or its suitability for any particular purpose.
 *
 *  You are free to use this software for any non-commercial educational or
 *  research purpose, provided that this copyright notice is not removed or
 *  modified. For commercial uses please contact the respective author(s).
 *
 *  If you find our software useful, we would appreciate if you mentioned it
 *  in any publication arising from the use of this software or acknowledge
 *  our work otherwise. We would also like to hear of any fixes or useful
 *  extensions to this software.
 *
 */

package unikl.disco.calculator.gui;

import unikl.disco.calculator.network.Vertex;
import unikl.disco.calculator.network.Network;
import unikl.disco.calculator.SNC;
import unikl.disco.calculator.symbolic_math.SymbolicFunction;
import unikl.disco.calculator.symbolic_math.functions.ConstantFunction;
import unikl.disco.calculator.symbolic_math.Service;
import unikl.disco.calculator.symbolic_math.BadInitializationException;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import unikl.disco.calculator.commands.AddVertexCommand;

/**
 * A dialog for editing a vertex.
 * @author Michael Beck
 *
 */
public class VertexEditor extends JDialog {

	//Members
	
	private static final long serialVersionUID = 4146247537102257784L;
	
	static final int CANCEL_OPTION = 0;
	static final int APPROVE_OPTION = 1;
	static final int ERROR_OPTION = 2;
	private int output = 0;
	private Vertex vertex;
        private final Network nw;
	
	private final JPanel topCardContainer = new JPanel();	
	
	//Constructors
	
	public VertexEditor(String title, Vertex vertex, Network nw, SNC snc){
		this(title, nw, snc);
		this.vertex = vertex;
	}
	
	public VertexEditor(String title, final Network nw, final SNC snc){
		this.nw = nw;
		//Constructs the dialog
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setTitle(title);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		//**************************************
		//Adds the textfield for giving an alias
		//**************************************
		JLabel aliasLabel = new JLabel("Alias of the vertex:");
		final JTextField aliasField = new JTextField(10);
		
		JPanel aliasPanel = new JPanel();
		aliasPanel.setLayout(new FlowLayout());
		aliasPanel.add(aliasLabel); aliasPanel.add(aliasField);
		
		mainPanel.add(aliasPanel);
		
		//******************
		//Adds the top panel
		//******************
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout());
		topPanel.add(new JLabel("Service Type: "));
		
		final String CONSTANT = "Constant rate service";

		String[] serviceBox = {CONSTANT};
		final JComboBox<String> service = new JComboBox<String>(serviceBox);
		topPanel.add(service);
		//Adds the listener for the initial arrival combo box
		ItemListener initListener = new ItemListener(){

			@Override
			public void itemStateChanged(ItemEvent e) {
				CardLayout card = (CardLayout) topCardContainer.getLayout();
				card.show(topCardContainer, (String) e.getItem());
			}			
		};
		service.addItemListener(initListener);
		
		//Adds the cards for the arrival
		topCardContainer.setLayout(new CardLayout());
		
		JPanel card1 = new JPanel();
		
		card1.add(new JLabel("Rate: c="));
		final JTextField constantRate = new JTextField(10);
		card1.add(constantRate);
		
		topCardContainer.add(card1, CONSTANT);
		
		mainPanel.add(topPanel);
		mainPanel.add(topCardContainer);
		((CardLayout) topCardContainer.getLayout()).show(topCardContainer, CONSTANT);

		//*********************
		//Adds the exit buttons
		//*********************
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent e) {
				output = APPROVE_OPTION;
				boolean correct = true;
                                double rate = 0.0;
	
				//Constant arrival case
				if(service.getSelectedItem() == CONSTANT){
					//uses the vertex-constructor to submit information to main GUI. Do not use this vertex directly!
					SymbolicFunction rho = null;
					try{
						rate = Double.valueOf(constantRate.getText());
						if(rate < 0) {
						    throw new BadInitializationException("Rate must be positive", rate);
						}
						rho = new ConstantFunction(-rate);
					}
					catch(BadInitializationException exc){	
						System.out.println("The rate must be positive.");
						rho = null;
						correct = false;
					}
					catch(NumberFormatException exc){
						System.out.println("The constant arrival rate must be a number.");
						rho = null;
						correct = false;
					} 
					Service service = new Service(new ConstantFunction(0), rho, nw);
					
					if(correct) {
                                            // Network ID is made up
                                            snc.invokeCommand(new AddVertexCommand(aliasField.getText(), -rate, 0, snc));
						vertex = new Vertex(-1, service, aliasField.getText(), nw);
						vertex.getService().getServicedependencies().clear();
					}
					else vertex = null;
				}
				
				
				dispose();
			}
		});
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				output = CANCEL_OPTION;
				dispose();
			}
			
		});
		buttonPanel.add(ok); buttonPanel.add(cancel);
		mainPanel.add(buttonPanel);
		
		//***************
		//Finishing touch
		//***************
		add(mainPanel);
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);		
	}
	
	//Methods
	
	public int showVertexEditor(){
		setVisible(true);
		return output;
	}
	
	//Getter and Setter
	
	public Vertex getEditedVertex(){
		return vertex;
	}
}
