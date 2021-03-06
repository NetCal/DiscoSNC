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

package org.networkcalculus.snc.commands;

import org.networkcalculus.snc.SNC;
import org.networkcalculus.snc.exceptions.NotImplementedException;

/**
 * Remove a vertex ({@link Vertex}) from a network.
 * 
 * @author Sebastian Henningsen
 */
public class RemoveVertexCommand implements Command {
	private final int vertexID;
	private final int networkID;
	private final SNC snc;
    
    /**
     * Constructs a new command to remove a vertex from a network.
     * 
     * @param vertexID The vertex ID
     * @param networkID The network ID the vertex belongs to
     * @param snc The overall controller
     */
    public RemoveVertexCommand(int vertexID, int networkID, SNC snc) {
    	this.vertexID = vertexID;
        this.networkID = networkID;
        this.snc = snc;
    }
    
    @Override
    public void execute() {
    	snc.getCurrentNetwork().removeVertex(snc.getCurrentNetwork().getVertex(vertexID));
    }

    @Override
    public void undo() {
    	// TODO
    	throw new NotImplementedException("Undo Operation for RemoveVertexCommand not implemented yet.");
    }
}
