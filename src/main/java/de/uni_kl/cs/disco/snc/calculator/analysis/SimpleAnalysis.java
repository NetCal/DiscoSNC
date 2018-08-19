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

package de.uni_kl.cs.disco.snc.calculator.analysis;

import java.util.Map;
import java.util.Stack;

import de.uni_kl.cs.disco.snc.calculator.network.ArrivalNotAvailableException;
import de.uni_kl.cs.disco.snc.calculator.network.Flow;
import de.uni_kl.cs.disco.snc.calculator.network.Network;
import de.uni_kl.cs.disco.snc.calculator.network.Vertex;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.AdditiveComposition;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.Arrival;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.BadInitializationException;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.Hoelder;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.NewParameter;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.Service;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.SymbolicFunction;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.functions.BFunction;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.functions.ConstantFunction;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.functions.ScaledFunction;
import de.uni_kl.cs.disco.utils.SetUtils;

/**
 * This kind of analysis computes successively the next output-
 * and service-bounds. For this an initial stack is built, which
 * contains all vertices, which have only established arrivals.
 * The first node is then popped and its output- and service
 * bounds are calculated. This might lead to the fact, that other
 * nodes now know all of their arrivals. Every time this is the 
 * case these nodes are pushed into the stack of vertices, which
 * know all of their arrivals. This procedure is continued until
 * the flow of interest (FoI) and the service of interest (SoI) 
 * is known or the stack is empty.
 * In the former case the wished bound is calculated from the SoI
 * and the FoI. In the latter case a non-feed-forward network was
 * given and hence the analysis throws a {@link DeadlockException}.
 * 
 * @author Michael Beck
 * 
 * @see AbstractAnalysis
 * @see DeadlockException
 */
public class SimpleAnalysis extends AbstractAnalysis {
	private Stack<Vertex> analyzableVertices;
	
	/**
	 * Constructs the Analysis-Object, with all information needed
	 * to give the wished performance bound at the flow and service
	 * of interest.
	 * 
     * @param nw
	 * @param vertices the set of vertices in the network
	 * @param flows the set of flows in the network
	 * @param vertex_of_interest the vertex at which the performance
	 * bound is calculated
	 * @param flow_of_interest the flow for which the performance
	 * bound is calculated
	 * @param boundtype the type of bound, which should be 
	 * calculated. Note: All performance bounds are given in 
	 * {@Arrival}-representation.
	 */
	public SimpleAnalysis(Network nw, Map<Integer,Vertex> vertices, Map<Integer,Flow> flows, int flow_of_interest, int vertex_of_interest, BoundType boundtype){
		super(nw, vertices, flows, flow_of_interest, vertex_of_interest, boundtype);
		analyzableVertices = new Stack<>();
	}
	
	/**
	 * Analyzes the network by successively computing left-over-
	 * service- and output-bounds.
	 * 
	 * @return The bound in the {@link Arrival}-representation.
	 * 
	 * @throws ArrivalNotAvailableException
	 * @throws DeadlockException
	 * @throws BadInitializationException
	 */
	@Override
	public Arrival analyze() throws ArrivalNotAvailableException, DeadlockException, BadInitializationException{
		
		// Initializes the stack of vertices, for which all arrivals are known
		for(Map.Entry<Integer, Vertex> entry : vertices.entrySet()){
			if(entry.getValue().canServe()) 	analyzableVertices.push(entry.getValue());
		}
		
		// Checks if the can_serve-stack is empty
		if(analyzableVertices.isEmpty()) throw new DeadlockException("The initial vertex stack is empty.");
		
		Vertex current_vertex;
		Arrival bound = new Arrival(nw);
		boolean successful = false;
		
		// Successively serves the flows until the FoI and SoI is characterized
		while(!analyzableVertices.isEmpty()){
			// Setup of service and flow
			current_vertex = analyzableVertices.pop();
			System.out.println("vertex "+current_vertex.getID()+" popped");
			int flowID = current_vertex.calculatePriority();
			Vertex next_vertex;

			// There might be no next vertex
			try{
				next_vertex = vertices.get(flows.get(flowID).getNextVertexID());
			}
			catch (IndexOutOfBoundsException e){
				next_vertex = null;
			}
			
			// Checks if the current vertex and flow are SoI and FoI respectively
			if(current_vertex.getID() == vertex_of_interest && flowID == flow_of_interest){
				bound = calculateBound(flows.get(flowID).getLastArrival(), current_vertex.getService());
				System.out.println("Arrival of interest found: "+flows.get(flowID).getLastArrival().toString());
				System.out.println("Service of interest found: "+current_vertex.getService().toString());
				successful = true;
				break;
			}
			
			// Calculates the output and sets the service in the vertex to the next leftover service
			Arrival output = current_vertex.serve();
			if(current_vertex.getAlias() != null) System.out.println("Flow with id "+flowID+" served at node "+current_vertex.getAlias());	
			else System.out.println("Flow with id "+flowID+" served at node "+current_vertex.getID());
			// There might be no next vertex
			try{
				flows.get(flowID).learnArrival(output);
			}
			catch (IndexOutOfBoundsException e){
			
			// There might be no next vertex
			}
			try{
				next_vertex.learnArrival(flowID, output);
				
				// pushes the next vertex if it knows all its arrivals
				if(next_vertex.canServe()) {
					analyzableVertices.push(next_vertex);
					System.out.println("Node "+next_vertex.getID()+" knows its arrivals and is pushed");
					}
			}
			catch (NullPointerException e){
				// TODO
			}
			
			// pushes the current vertex if it has more flows to serve
			if(current_vertex.canServe()){ analyzableVertices.push(current_vertex); System.out.println("Vertex "+current_vertex.getID()+" pushed");}
		}
		
		// checks if the FoI and SoI had been calculated
		if(successful == false) throw new DeadlockException("Flow of Interest or Arrival of Interest can't be calculated. Non-Feed-Forward-Network?");
		
		return bound;
	}
	
	/**
	 * Helper function for {@link analysis()}. It takes the FoI and
	 * SoI and calculated the performance bound. Stochastic 
	 * dependencies are taken into account and might increase the
	 * number of Hoelder-coefficients.
	 * 
	 * @param arrival the {@link Arrival}-description of the FoI
	 * @param service the {@link Service}-description of the SoI
	 * 
	 * @return the bound in the {@link Arrival}-description
	 * 
	 * @throws BadInitializationException
	 */
	private Arrival calculateBound(Arrival arrival, Service service) throws BadInitializationException{
		Arrival result;
		
		// The result is dependent on the wished performance-bound
		switch(getBoundType()){
		case BACKLOG:
			SymbolicFunction preparation;
			
			// Dependent Case
			if(!SetUtils.getIntersection(arrival.getServicedependencies(),service.getServicedependencies()).isEmpty() || !SetUtils.getIntersection(service.getArrivaldependencies(), arrival.getArrivaldependencies()).isEmpty()){
				Hoelder hoelder = nw.createHoelder();
				preparation = new AdditiveComposition(new AdditiveComposition(arrival.getSigma(),service.getSigma(),hoelder), 
						new BFunction(new AdditiveComposition(arrival.getRho(),service.getRho(),hoelder)));
			}
			
			// Independent Case
			else{
				preparation = new AdditiveComposition(new AdditiveComposition(arrival.getSigma(),service.getSigma()), 
												new BFunction(new AdditiveComposition(arrival.getRho(),service.getRho())));	
			}
			
			// introduces the backlog-part in the backlog-bound as new variable. The sign must be negative!
			SymbolicFunction backlog_part = new NewParameter(nw.createHoelder());
			SymbolicFunction function = new AdditiveComposition(preparation, backlog_part);

			// In the vector of variables the backlog has Hoelder_ID equal to Network.HOELDER_ID-1.
			result = new Arrival(function,new ConstantFunction(0), nw);
			break;
		
		case DELAY:
			SymbolicFunction sigma;
			SymbolicFunction rho;
	/* 
		Debbugging:
			System.out.println("Service dependencies of AoI:"+arrival.getServicedependencies().toString());
			System.out.println("Service dependencies of SoI:"+service.getServicedependencies().toString());
			System.out.println("Arrival dependencies of AoI:"+arrival.getArrivaldependencies().toString());
			System.out.println("Arrival dependencies of SoI:"+service.getArrivaldependencies().toString());
	*/
			// Dependent Case
			if(!SetUtils.getIntersection(arrival.getServicedependencies(),service.getServicedependencies()).isEmpty() || !SetUtils.getIntersection(service.getArrivaldependencies(), arrival.getArrivaldependencies()).isEmpty()){
				Hoelder hoelder = nw.createHoelder();
				SymbolicFunction prep1 = new AdditiveComposition(arrival.getSigma(),service.getSigma(),hoelder);
				SymbolicFunction prep2 = new AdditiveComposition(arrival.getRho(),service.getRho(),hoelder);
				
				sigma = new AdditiveComposition(prep1,new BFunction(prep2));
				rho = new ScaledFunction(service.getRho(), hoelder, false);
				System.out.println("Dependent case");
			}
			
			// Independent Case
			else{
				sigma = new AdditiveComposition(new AdditiveComposition(arrival.getSigma(),service.getSigma()),
						new BFunction(new AdditiveComposition(arrival.getRho(),service.getRho())));
				rho = service.getRho();
				System.out.println("Independent Case");
			} 
			result = new Arrival(sigma, rho, nw);
			break;
		
		case OUTPUT:
			SymbolicFunction givensigma;
			SymbolicFunction givenrho;
			
			// Dependent Case
			if(!SetUtils.getIntersection(arrival.getServicedependencies(),service.getServicedependencies()).isEmpty() || !SetUtils.getIntersection(service.getArrivaldependencies(), arrival.getArrivaldependencies()).isEmpty()){
				Hoelder hoelder = nw.createHoelder();
				givensigma = new AdditiveComposition(new AdditiveComposition(arrival.getSigma(),service.getSigma(),hoelder),new BFunction(new AdditiveComposition(arrival.getRho(),service.getRho(),hoelder)));
				givenrho = new ScaledFunction(arrival.getRho(),hoelder, false);
			}
			
			// Independent Case
			else{
				givensigma = new AdditiveComposition(new AdditiveComposition(arrival.getSigma(),service.getSigma()),new BFunction(new AdditiveComposition(arrival.getRho(),service.getRho())));
				givenrho = arrival.getRho();
			}
			result = new Arrival(givensigma, givenrho, nw);
			break;
		
		default:
			result = new Arrival(nw);
		}
		
		return result;
	}
}
