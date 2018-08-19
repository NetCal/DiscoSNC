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

package de.uni_kl.cs.disco.snc.calculator.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_kl.cs.disco.snc.calculator.SNC;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.Arrival;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.ArrivalFactory;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.BadInitializationException;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.Hoelder;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.Service;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.ServiceFactory;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.SymbolicFunction;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.functions.ConstantFunction;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.functions.EBBSigma;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.functions.ExponentialSigma;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.functions.StationaryTBSigma;
import de.uni_kl.cs.disco.snc.exceptions.FileOperationException;

/**
 * This class provides several methods to construct and change a network
 * consisting of {@link Flow}s and {@link vertex}-objects. It has two static
 * variables, which are used to automatically tag new nodes and flows with
 * distinct IDs.
 *
 * @author Michael Beck
 * @author Sebastian Henningsen
 * 
 * @see Flow
 * @see Vertex
 * @see Arrival
 * @see Service
 * @see Hoelder
 */
public class Network implements Serializable {
	private static final long serialVersionUID = 695224731594099768L;
    
    private Map<Integer, Vertex> vertices;
    private Map<Integer, Flow> flows;
    private Map<Integer, Hoelder> hoelders;
	
	private int FLOW_ID;
    private int VERTEX_ID;
    private int HOELDER_ID;
    
    private List<NetworkListener> listeners;

    public Network() {
        this(null, null, null);
    }

    public Network(Map<Integer, Vertex> vertices, Map<Integer, Flow> flows, Map<Integer, Hoelder> hoelders) {
    	this.vertices = (vertices != null) ? vertices : new HashMap<Integer, Vertex>();
        this.flows = (flows != null) ? flows : new HashMap<Integer, Flow>();
        this.hoelders = (hoelders != null) ? hoelders : new HashMap<Integer, Hoelder>();
        
        FLOW_ID = this.flows.size() + 1;
        VERTEX_ID = this.vertices.size() + 1;
        HOELDER_ID = this.hoelders.size() + 1;
        
        this.listeners = new ArrayList<NetworkListener>();
    }

    public boolean addListener(NetworkListener l) {
        return listeners.add(l);
    }

    public boolean removeListener(NetworkListener l) {
        return listeners.remove(l);
    }

    /**
     * Creates a new Hoelder-Object and returns its id.
     *
     * @return the newly created Hoelder-Object.
     */
    public Hoelder createHoelder() {
        Hoelder hoelder = new Hoelder(HOELDER_ID);
        hoelders.put(hoelder.getHoelderID(), hoelder);
        incrementHOELDER_ID();
        return hoelder;
    }

    /**
     * Adds a new dummy vertex with alias
     *
     * @param alias
     */
    public void addVertex(String alias) {
        Vertex vertex = new Vertex(VERTEX_ID, alias, this);
        vertices.put(VERTEX_ID, vertex);
        incrementVERTEX_ID();
        for (NetworkListener l : listeners) {
            l.vertexAdded(vertex);
        }
    }

    /**
     * Adds a new vertex with predefined service to the network
     *
     * @param service the service the new vertex possesses
     */
    public void addVertex(Service service) {
        addVertex(service, "");
    }

    /**
     * Adds a new vertex with predefined service and alias
     *
     * @param service
     * @param alias
     * @return
     */
    public Vertex addVertex(Service service, String alias) {
        Vertex vertex = new Vertex(VERTEX_ID, service, alias, this);
        vertices.put(VERTEX_ID, vertex);
        incrementVERTEX_ID();
        for (NetworkListener l : listeners) {
            l.vertexAdded(vertex);
        }
        return vertex;
    }

    /**
     * Adds a vertex with a given ID, overwrites any existing vertices
     *
     * @param vertex
     * @return
     */
    public Vertex addVertex(Vertex vertex) {
        vertices.put(vertex.getID(), vertex);
        for (NetworkListener l : listeners) {
            l.vertexAdded(vertex);
        }
        return vertex;
    }

    /**
     * Computes the leftover service at the vertex with the given ID. This
     * serves the prioritized flow and removes it from the arrivals of the node.
     *
     * @param vertexID
     * 
     * @return The output bound of the flow being served
     * @throws ArrivalNotAvailableException
     */
    public Arrival computeLeftoverService(int vertexID) throws ArrivalNotAvailableException {
        int fid = getVertex(vertexID).getPrioritizedFlow();
        Arrival output = getVertex(vertexID).serve();
        // Notify listeners
        for (NetworkListener l : listeners) {
            l.vertexChanged(getVertex(vertexID));
            l.flowChanged(getFlow(fid));
        }
        return output;
    }

    public int convolute(int vertex1ID, int vertex2ID, int flowOfInterestID) {
        // Compute convoluted service, add a new vertex with that service
        // Some preconditions have to be met: 
        // (1) The vertices are direct neighbours wrt. to the flow of interest (FoI)
        // (2) There are no other flows on the path of the FoI
        if (areConvolutable(vertex1ID, vertex2ID, flowOfInterestID)) {
            Vertex v1 = getVertex(vertex1ID);
            Vertex v2 = getVertex(vertex2ID);
            Flow foi = getFlow(flowOfInterestID);
            Service convService = v1.getService().concatenate(v1.getService(), v2.getService());
            Vertex convVertex = addVertex(convService, vertex1ID + " conv. " + vertex2ID);
            Arrival arrival = v1.getArrivalOfFlow(flowOfInterestID);
            convVertex.addArrival(v1.getPriorityOfFlow(flowOfInterestID), flowOfInterestID, arrival);
            foi.replaceFirstOccurence(v1.getID(), v2.getID(), convVertex);

        /*
             Set<Integer> v1Flows = v1.getAllFlowIDs();
             Set<Integer> v2Flows = v2.getAllFlowIDs();

             for (Integer flowID : v1Flows) {
             // TODO: This is not very nice
             Arrival arrival = v1.getArrivalOfFlow(flowID);
             if (arrival != null) {
             convVertex.addArrival(v1.getPriorityOfFlow(flowID), flowID, arrival);
             } else {
             convVertex.addUnknownArrival(v1.getPriorityOfFlow(flowID), flowID);
             }
             Flow f = getFlow(flowID);
             f.replaceFirstOccurence(v1.getID(), v2.getID(), convVertex);
             }

             for (Integer flowID : v2Flows) {
             Arrival arrival = v2.getArrivalOfFlow(flowID);
             if (arrival != null) {
             convVertex.addArrival(v2.getPriorityOfFlow(flowID), flowID, arrival);
             } else {
             convVertex.addUnknownArrival(v2.getPriorityOfFlow(flowID), flowID);
             }
             // TODO: Make priorities unique again
             Flow f = getFlow(flowID);
             f.replaceFirstOccurence(v1.getID(), v2.getID(), convVertex);
             }
        */
            removeVertex(v1);
            removeVertex(v2);
            return convVertex.getID();
        } else {
            throw new NetworkActionException("Vertices are not convolutable!");
        }
    }

    public boolean areConvolutable(int vertex1ID, int vertex2ID, int flowID) {
        return true;
    }

    /**
     * Sets the service at a specific node in the network
     *
     * @param vertex the vertex to be altered
     * @param service the new service at the specific vertex
     */
    public void setServiceAt(Vertex vertex, Service service) {
        vertex.setMGFService(service);
    }

    /**
     * Sets the service at the vertex with the given id
     *
     * @param vertex_id the id of the vertex to be altered
     * @param service the new service at the specific vertex
     */
    public void setServiceAt(int vertex_id, Service service) {
        vertices.get(vertex_id).setMGFService(service);
    }

    /**
     * Removes a vertex from the network. This is done by removing the vertex
     * from all flows, which include this vertex in its route. I.e. if a flow
     * routes through A-B-C and B is removed the new route will be A-C. After
     * this the vertex is removed from the list of vertices.
     *
     * @param vertex the <code>Vertex</code> to be removed
     * @return if removing the vertex was successful
     */
    public boolean removeVertex(Vertex vertex) {
        return removeVertex(vertex.getID());
    }

    public boolean removeVertex(int id) {
        boolean success = false;
        Vertex vertex = getVertex(id);
        if (vertices.containsKey(id)) {
            for (int i : getVertex(id).getAllFlowPriorities().keySet()) {
                flows.get(i).removeVertex(id);
                for (NetworkListener l : listeners) {
                    l.flowChanged(getFlow(i));
                }
            }
            vertices.remove(id);
            success = true;
        }
        // Notify listeners
        for (NetworkListener l : listeners) {
            l.vertexRemoved(vertex);
        }
        return success;
    }

    /**
     * Creates a new flow with an initial arrival and a complete description of
     * its route through the network (in expression all of its vertices and the
     * corresponding priorities at these vertices of the flow).
     *
     * @param initial_arrival the arrival at the first node
     * @param route
     * @param vertices the vertices the flow traverses
     * @param priorities the priorities of the flow at the corresponding
     * vertices
     * @param alias the alias of the new flow
     * 
     * @return
     * 
     * @throws ArrivalNotAvailableException
     */
    public int addFlow(Arrival initial_arrival, List<Integer> route, List<Integer> priorities,
            String alias) throws ArrivalNotAvailableException {

        // Creates the dummy arrivals for all vertices after the first
        List<Arrival> arrivals = new ArrayList<>(1);
        arrivals.add(0, initial_arrival);
        for (int i = 1; i < route.size(); i++) {
            arrivals.add(new Arrival(this));
        }

        // Adds the flow into the flow list
        Flow flow = new Flow(FLOW_ID, route, arrivals, priorities, alias, this);
        // Check whether every vertex exists
        for (int i = 0; i < route.size(); i++) {
            if (vertices.get(route.get(i)) == null) {
                throw new NetworkActionException("Error while adding flow " + alias + ". No node with ID " + i);
            }
        }
        // Initializes the first arrival at the first vertex
        Vertex first_vertex = vertices.get(route.get(0));

        flows.put(FLOW_ID, flow);
        
        // Writes the flow in its corresponding vertices
        for (int i = 0; i < route.size(); i++) {
            Vertex vertex;
            vertex = vertices.get(route.get(i));
            vertex.addUnknownArrival(priorities.get(i), FLOW_ID);
        }
        first_vertex.learnArrival(FLOW_ID, initial_arrival);

        // Increments the flow count
        incrementFLOW_ID();

        // Notify the listeners
        for (NetworkListener l : listeners) {
            l.flowAdded(flow);
        }
        return flow.getID();
    }

    /**
     * Appends a node to an already existing flow. The arrival at this appended
     * node is non established.
     *
     * @param flow_id the flow ot which the node is appended
     * @param vertex_id the vertex which is appended
     * @param priority the priority the flow has at the appended vertex.
     */
    public void appendNode(int flow_id, int vertex_id, int priority) {
        // Adds the vertex to the path of the flow
        flows.get(flow_id).addNodetoPath(vertex_id, priority);

        // Adds a non-established arrival to the appended vertex
        vertices.get(vertex_id).addUnknownArrival(priority, flow_id);
    }

    /**
     * Sets the initial arrival of some flow
     *
     * @param flow_id the flow-id of the flow, which is initialized
     * @param arrival the initial arrival
     * 
     * @throws ArrivalNotAvailableException
     */
    public void setInitialArrival(int flow_id, Arrival arrival) throws ArrivalNotAvailableException {
        // Initializes the arrival at the flow
        flows.get(flow_id).setInitialArrival(arrival);

        // The arrival is established at the vertex
        Vertex vertex = vertices.get(flows.get(flow_id).getFirstVertexID());
        vertex.learnArrival(flow_id, arrival);
    }

    /**
     * Removes a flow from the network. This is done by deleting the
     * corresponding entries in the vertices, which lie on the route of the
     * flow. After this the flow itself is removed from <code>flows</code>.
     *
     * @param flow the <code>Flow</code> to be removed
     * 
     * @return returns if removing the flow was successful
     */
    public boolean removeFlow(Flow flow) {
        boolean success = false;
        if (flows.containsKey(flow.getID())) {
            for (int i : flow.getVerticeIDs()) {
                vertices.get(i).removeFlow(flow.getID());
            }
            flows.remove(flow.getID());
            success = true;

        }
        // Notify the listeners
        for (NetworkListener l : listeners) {
            l.flowRemoved(flow);
        }
        return success;
    }

    private void incrementFLOW_ID() {
        FLOW_ID++;
    }

    private void incrementVERTEX_ID() {
        VERTEX_ID++;
    }

    private void incrementHOELDER_ID() {
        HOELDER_ID++;
    }

    public void resetFLOW_ID(int reset) {
        FLOW_ID = reset;
    }

    public void resetVERTEX_ID(int reset) {
        VERTEX_ID = reset;
    }

    public void resetHOELDER_ID(int reset) {
        HOELDER_ID = reset;
    }

    /**
     * Returns a string representation of the network. This is done by first
     * listing the vertices aliases or if not existend their IDs.
     *
     * @return
     */
    public String getStringRepresentation() {
		StringBuffer network_str = new StringBuffer();
		
		network_str.append("List of vertices:");
		network_str.append("\n");
        for (Map.Entry<Integer,Vertex> entry : vertices.entrySet()) {
    		network_str.append("Vertex-ID: ");
    		network_str.append(Integer.toString(entry.getValue().getID()));
    		network_str.append("\t");
    		network_str.append(" Vertex-Alias: ");
    		network_str.append(entry.getValue().getAlias());
    		network_str.append("\n");
        }
        
		network_str.append("List of flows:");
		network_str.append("\n");
        for (Map.Entry<Integer,Flow> entry : flows.entrySet()) {
    		network_str.append("Flow-ID: ");
    		network_str.append(Integer.toString(entry.getValue().getID()));
    		network_str.append("\t");
    		network_str.append(" Flow-Alias: ");
    		network_str.append(entry.getValue().getAlias());
    		network_str.append("\t");
    		network_str.append(" route:");
    		network_str.append("\n");
    		network_str.append(entry.getValue().getVerticeIDs().toString());
        }

		network_str.append("Number of Hoelder parameters: ");
		network_str.append(Integer.toString(HOELDER_ID - 1));

		return network_str.toString();
    }

    public Vertex getVertex(int id) {
        return vertices.get(id);
    }

    public Vertex getVertexByName(String name) {
        for (Map.Entry<Integer, Vertex> entry : vertices.entrySet()) {
            Vertex vertex = entry.getValue();
            if (vertex.getAlias().equals(name)) {
                return vertex;
            }
        }
        return null;
    }

    public Flow getFlow(int id) {
        return flows.get(id);
    }

    public int getVERTEX_ID() {
        return VERTEX_ID;
    }

    public int getFLOW_ID() {
        return FLOW_ID;
    }

    public int getHOELDER_ID() {
        return HOELDER_ID;
    }

    public Map<Integer, Vertex> getVertices() {
        return vertices;
    }

    public Network deepCopy() {
        Map<Integer, Vertex> newVertices = new HashMap<Integer, Vertex>(this.vertices.size());
        Map<Integer, Flow> newFlows = new HashMap<Integer, Flow>(this.flows.size());
        Map<Integer, Hoelder> newHoelders = new HashMap<Integer, Hoelder>(this.hoelders.size());

        for (Map.Entry<Integer, Vertex> entry : vertices.entrySet()) {
            Vertex newVertex = entry.getValue().copy();
            newVertices.put(entry.getKey(), newVertex);
        }

        for (Map.Entry<Integer, Flow> entry : flows.entrySet()) {
            Flow newFlow = entry.getValue().copy();
            newFlows.put(entry.getKey(), newFlow);
        }

        for (Map.Entry<Integer, Hoelder> entry : hoelders.entrySet()) {
            Hoelder newHoelder = entry.getValue().copy();
            newHoelders.put(entry.getKey(), newHoelder);
        }
        return new Network(newVertices, newFlows, newHoelders);
    }

    /**
     * Loads a network, which is given in <code>file</code>. Can only read
     * networks, which had been saved by a simple
     * <code>ObjectOutputStream</code>. The order of saved objects (and its
     * corresponding type) is: vertices (HashMap<Integer, Vertex>
     * flows (HashMap<Integer, Flow>) hoelders (HashMap<Integer, Hoelder>)
     *
     * @param profile_path
     * @param redirectListeners
     * 
     * @return
     */
    public static Network load(File profile_path, boolean redirectListeners) {
        // will read profile.txt line by line
        Network nw = new Network();
        // Kind of a hack
        if (redirectListeners) {
            List<NetworkListener> oldListeners = SNC.getInstance().getCurrentNetwork().getListeners();
            for (NetworkListener l : oldListeners) {
                l.clear();
                nw.addListener(l);
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(profile_path))) {
            String sCurrentLine;

            // reads all lines
            while ((sCurrentLine = br.readLine()) != null) {
                // If a line starts with "I" an interface (i.e. service element) is added to the network in form of a Vertex.
                if (sCurrentLine.startsWith("I")) {
                    try {
                        nw.handleVertexLine(sCurrentLine);
                    } catch (BadInitializationException | NumberFormatException e) {
                        nw.clearListeners(redirectListeners);
                        //"Parameter for constant rate server must be a non-negative number."
                        throw new FileOperationException(e.getMessage(), sCurrentLine);
                    }
                }

                // If a line starts with "F" a flow is added to the network in form of a Flow-object.
                if (sCurrentLine.startsWith("F")) {
                    try {
                        nw.handleFlowLine(sCurrentLine);
                    } catch (BadInitializationException | ArrivalNotAvailableException | FileOperationException e) {
                        nw.clearListeners(redirectListeners);
                        throw new FileOperationException(e.getMessage(), sCurrentLine);
                    }
                }
            }
            System.out.println("The following vertices had been added (alias, rate):");
            for (Vertex vertex : nw.getVertices().values()) {
                System.out.println(vertex.getAlias() + ", " + vertex.getService().toString());
            }
            System.out.println("The following flows had been added (alias, route, priorities):");
            for (Flow flow : nw.getFlows().values()) {
                System.out.println(flow.getAlias() + ", " + flow.getVerticeIDs() + ", " + flow.getPriorities());
            }
            br.close();
        } catch (IOException e) {
            // Will be automatically closed due to try-with-resources
            // Rewrap the exception and propagate it upwards
            throw new FileOperationException(e);
        }
        return nw;
    }

    private void clearListeners(boolean redirectListeners) {
        if (redirectListeners) {
            List<NetworkListener> oldListeners = SNC.getInstance().getCurrentNetwork().getListeners();
            for (NetworkListener l : oldListeners) {
                l.clear();
            }
        }
    }

    private int handleVertexLine(String line) throws BadInitializationException {
        // Removes the first character, we do not need that anymore anyway
        line = line.substring(1).trim();
        String[] lineParts = line.split(",");
        String vertex_name = lineParts[0].trim();
        Double service_rate = Double.parseDouble(lineParts[3].trim());
        return this.addVertex(ServiceFactory.buildConstantRate(-service_rate), vertex_name).getID();
    }

    private void handleFlowLine(String line) throws NumberFormatException, BadInitializationException, ArrivalNotAvailableException {
        final int pathOffset = 2; // There are 2 entries before the route
        
        // Removes the first character, we do not need that anoymore
        line = line.substring(1).trim();
        String[] lineParts = line.split(",");
        String flowName = lineParts[0].trim();
        int pathLength = Integer.parseInt(lineParts[1].trim());
        // We know that path_length entries in lineParts are only relevant for the route
        List<Integer> route = new ArrayList<>();
        List<Integer> priorities = new ArrayList<>();
        for (int i = 0; i < pathLength; i++) {
            String[] entry = lineParts[i + pathOffset].trim().split(":");
            Vertex v = this.getVertexByName(entry[0].trim());
            if (v == null) {
                throw new FileOperationException("Could not find Vertex " + entry[0].trim(), line);
            }
            route.add(this.getVertexByName(entry[0].trim()).getID());
            priorities.add(Integer.parseInt(entry[1].trim()));
        }

        // Find out the Arrival Type now. It's located after the path
        String arrivalType = lineParts[pathOffset + pathLength].trim();
        Arrival arrival = null;
        if (arrivalType.equals("EBB")) {
            double rate;
            double decay;
            double prefactor;
            // TODO: Error Handling.
            rate = Double.parseDouble(lineParts[pathOffset + pathLength + 1].trim());
            decay = Double.parseDouble(lineParts[pathOffset + pathLength + 2].trim());
            prefactor = Double.parseDouble(lineParts[pathOffset + pathLength + 3].trim());
            arrival = ArrivalFactory.buildEBB(rate, decay, prefactor);

        } else if (arrivalType.equals("CONSTANT")) {
            double rate = Double.parseDouble(lineParts[pathOffset + pathLength + 1].trim());
            arrival = ArrivalFactory.buildConstantRate(rate);
        } else if (arrivalType.equals("EXPONENTIAL")) {
            double rate = Double.parseDouble(lineParts[pathOffset + pathLength + 1].trim());
            arrival = ArrivalFactory.buildExponentialRate(rate);
        } else if (arrivalType.equals("STATIONARYTB")) {
            double rate;
            double bucket;
            double maxTheta;
            // Check if there are 2 or 3 parameters given
            if (lineParts.length - pathOffset - pathLength - 1 == 2) {
                rate = Double.parseDouble(lineParts[pathOffset + pathLength + 1].trim());
                bucket = Double.parseDouble(lineParts[pathOffset + pathLength + 2].trim());
                arrival = ArrivalFactory.buildStationaryTB(rate, bucket);
            } else {
                rate = Double.parseDouble(lineParts[pathOffset + pathLength + 1].trim());
                bucket = Double.parseDouble(lineParts[pathOffset + pathLength + 2].trim());
                maxTheta = Double.parseDouble(lineParts[pathOffset + pathLength + 3].trim());
                arrival = ArrivalFactory.buildStationaryTB(rate, bucket, maxTheta);
            }
        } else {
            throw new FileOperationException("No arrival with type " + arrivalType + " known.", line);
        }
        int flowID = this.addFlow(arrival, route, priorities, flowName);
        this.getFlow(flowID).getInitialArrival().getArrivaldependencies().clear();
    }

    /**
     * Saves the network in the given <code>file</code>. This is done by using a
     * simple ObjectOutputStream. The order of saved objects (and its
     * corresponding type) is: vertices (HashMap<Integer, Vertex>
     * flows (HashMap<Integer, Flow>) hoelders (HashMap<Integer, Hoelder>)
     * Throws a runtime FileOperationException if an error occurs
     *
     * @param file
     */
    public void save(File file) {
        if (this.getHOELDER_ID() > 1) {
            throw new FileOperationException("Currently not possible to store networks with Hölder IDs");
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            // Write out the nodes
            for (Vertex v : this.getVertices().values()) {
                bw.write("I " + v.getAlias() + ", " + "FIFO" + ", " + "CR" + ", " + v.getService().getRho().toString().substring(1));
                bw.newLine();
            }
            bw.write("EOI");
            bw.newLine();

            for (Flow f : this.getFlows().values()) {
                List<Integer> route = f.getVerticeIDs();
                List<Integer> priorities = f.getPriorities();
                StringBuilder outRoute = new StringBuilder();
                for (int i = 0; i < route.size(); i++) {
                    outRoute.append(this.getVertices().get(route.get(i)).getAlias());
                    outRoute.append(":");
                    outRoute.append(priorities.get(i));
                    if (i < route.size() - 1) {
                        outRoute.append(", ");
                    }
                }
                Arrival initArrival = f.getInitialArrival();
                // Until we use a parser for symbolic functions: Find out what kind of Arrival this is
                // Since the initial arrival stems from a text file there are only 5 options to choose from
                SymbolicFunction rho = initArrival.getRho();
                SymbolicFunction sigma = initArrival.getSigma();
                String arrivalParameters = "";
                if (rho instanceof ConstantFunction && sigma instanceof ConstantFunction) {
                    arrivalParameters = "CONSTANT, " + rho.toString();
                    // Constant Arrival
                } else if (rho instanceof ConstantFunction && sigma instanceof EBBSigma) {
                    arrivalParameters = "EBB, " + rho.toString() + ", " + sigma.toString().substring(4, sigma.toString().length() - 1);
                } else if (rho instanceof ConstantFunction && sigma instanceof StationaryTBSigma) {
                    arrivalParameters = "STATIONARYTB, " + rho.toString() + ", " + sigma.toString().substring(7, sigma.toString().length() - 1);
                    if (sigma.getmaxTheta() != Double.POSITIVE_INFINITY) {
                        arrivalParameters += ", " + sigma.getmaxTheta();
                    }
                } else if (rho instanceof ExponentialSigma && sigma instanceof ConstantFunction) {
                    arrivalParameters = "EXPONENTIAL, " + rho.toString().substring(8, rho.toString().length() - 1);
                } else {
                    throw new FileOperationException("Save Network: No matching arrival types found for flow " + f.getAlias());
                }
                bw.write("F " + f.getAlias() + ", " + route.size() + ", " + outRoute + ", " + arrivalParameters);
                bw.newLine();
            }
            bw.write("EOF");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            // Since we are using try-with-resources, the file will be closed automatically
            // when an exception occurs.
            throw new FileOperationException(e);
        }
    }

    public Map<Integer, Flow> getFlows() {
        return flows;
    }

    public Map<Integer, Hoelder> getHoelders() {
        return hoelders;
    }

    public List<NetworkListener> getListeners() {
        return listeners;
    }
}
