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

package de.uni_kl.cs.disco.snc.calculator.commands;

import de.uni_kl.cs.disco.snc.calculator.SNC;
import de.uni_kl.cs.disco.snc.calculator.network.ArrivalNotAvailableException;
import de.uni_kl.cs.disco.snc.calculator.network.NetworkActionException;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.Arrival;

import java.util.List;

/**
 * Add a {@link Flow} with given properties to the target network.
 * 
 * @author Sebastian Henningsen
 */
public class AddFlowCommand implements Command {
	
    private final String alias;
    int networkID;
    SNC snc;
    boolean success;
    int flowID;
    Arrival arrival;
    List<Integer> route;
    List<Integer> priorities;
    
    /**
     * Creates a new AddFlowCommand 
     * 
     * @param alias The name of the new flow
     * @param arrival The initial arrival of the new flow
     * @param route The route the new flow, a list of vertexIDs
     * @param priorities The priorities along the route 
     * @param networkID The network this flow belongs to
     * @param snc The overall controller
     */
    public AddFlowCommand(String alias, Arrival arrival, List<Integer> route, List<Integer> priorities, int networkID, SNC snc) {
        this.alias = alias != null ? alias : "";
        this.networkID = networkID;
        this.snc = snc;
        this.success = false;
		this.arrival = arrival;
		this.route = route;
		this.priorities = priorities;
    }
    
    @Override
    public void execute() {
		try {
		    flowID = snc.getCurrentNetwork().addFlow(arrival, route, priorities, alias);
		    // TODO: Why is this?
		    snc.getCurrentNetwork().getFlow(flowID).getInitialArrival().getArrivaldependencies().clear();
	
		} catch (ArrivalNotAvailableException e) {
			success = false;
	        throw new NetworkActionException("Error while adding flow " + this.alias + ": " + e.getMessage());
		}
    }

    @Override
    public void undo() {
        if(success) {
            snc.getCurrentNetwork().removeFlow(snc.getCurrentNetwork().getFlow(flowID));
        }
    }
}
