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

import de.uni_kl.cs.disco.snc.calculator.analysis.BoundType;
import de.uni_kl.cs.disco.snc.calculator.analysis.AnalysisException;

/**
 * A factory to abstract from different implementations of the @link Optimizer interface.
 * 
 * @author Sebastian Henningsen
 */
public class OptimizationFactory {
    /**
     * Creates an instance of the chosen @link OptimizationType
     *  
     * @param nw The network the optimizer is associated with
     * @param bound The bound-to-be-optimized
     * @param boundtype A leftover, will be removed in future versions
     * @param type The desired OptimizationType
     * 
     * @return A concrete instance of an optimizer, which is chosen w.r.t. the passed OptimizationType enum.
     */
    public static Optimizer getOptimizer(Optimizable bound, BoundType boundtype, OptimizationType type) {
        switch(type) {
            case SIMPLE_OPT:
                return new SimpleOptimizer(bound, boundtype);
            case GRADIENT_OPT:
                return new SimpleGradient(bound, boundtype);
            default:
                throw new AnalysisException("Optimization Type: " + type.toString() + " not known.");
        }
    }
}
