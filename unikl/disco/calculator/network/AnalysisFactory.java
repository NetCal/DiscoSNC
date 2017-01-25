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

package unikl.disco.calculator.network;

import java.util.Map;
import unikl.disco.misc.AnalysisException;

/**
 *
 * @author Sebastian Henningsen
 */
public class AnalysisFactory {
    
    /**
     *
     * @param type
     * @param nw
     * @param vertices
     * @param flows
     * @param flow_of_interest
     * @param vertex_of_interest
     * @param boundtype
     * @return
     */
    public static Analyzer getAnalyzer(AnalysisType type, Network nw, Map<Integer, Vertex> vertices, Map<Integer, Flow> flows, int flow_of_interest, int vertex_of_interest, AbstractAnalysis.Boundtype boundtype) {
        switch(type) {
            case SIMPLE_ANA:
                return new SimpleAnalysis(nw, vertices, flows, flow_of_interest, vertex_of_interest, boundtype);
            case LADDER_ANA:
            	return new LadderAnalysis(nw, vertices, flows, flow_of_interest, vertex_of_interest, boundtype);
            default:
                throw new AnalysisException("Analysis Type: " + type.toString() + " not known.");
        }
    }
}
