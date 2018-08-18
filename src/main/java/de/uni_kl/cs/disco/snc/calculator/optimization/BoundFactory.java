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

package de.uni_kl.cs.disco.snc.calculator.optimization;

import de.uni_kl.cs.disco.snc.calculator.symbolic_math.Arrival;
import de.uni_kl.cs.disco.snc.exceptions.AnalysisException;

/**
 * A factory for bound which can be optimized. The goal is to add an extra layer
 * between bounds and optimization in order to provide a generic and simple interface
 * for bound handling and creation.
 * 
 * @author Sebastian Henningsen
 */
public class BoundFactory {

    /**
     * Creates the bound which is specified by boundtype
     * 
     * @param input The arrival on which a bound shall be provided
     * @param boundtype The desired bound type
     * @param bound A bound value, for example a violation probability or a maximum backlog value.
     * 
     * @return A suitable bound according to the inputs
     */
    public static Optimizable createBound(Arrival input, BoundType boundtype, double bound) {
        switch(boundtype) {
            case BACKLOG:
                return new BacklogBound(input, bound);
            case DELAY:
                return new DelayBound(input, bound);
            case INVERSE_BACKLOG:
                return new InverseBacklogBound(input, bound);
            case INVERSE_DELAY:
                return new InverseDelayBound(input, bound);
            default:
                throw new AnalysisException("Bound type " + boundtype.toString() + " not known.");
        }
    }
}
