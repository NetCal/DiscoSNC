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
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.Arrival;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.ParameterMismatchException;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.ServerOverloadException;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.ThetaOutOfBoundException;

/**
 * This performs the numerical part of optimizing the bound
 * found by the analysis. For this a good combination of parameter-
 * values for the <code>input</code> is found and by this a 
 * near optimal bound on violation probabilities calculated.
 * Important is also the "reverse" operation, which finds the
 * nearly optimal bound for a given violation probability.
 * A calculation of output bounds is not needed.
 * This class should be used as a starting point when writing
 * own implementations of the @link Optimizer interface
 * 
 * @author Michael Beck
 * @author Sebastian Henningsen
 */
public abstract class AbstractOptimizer implements Optimizer {
    /**
     * The bound-to-be-optimized
     */
    protected Optimizable bound;

    /**
     * A leftover, this information is now encapsulated into the implementations of @link Optimizable
     */
    protected BoundType boundtype;

    /**
     * The maximum value of theta
     */
    protected double maxTheta;

     /**
     * Sets the basic parameters
     * 
     * @param input Bound to-be-optimized
     * @param boundtype Leftover, will be removed in future versions
     * @param nw The corresponding network the optimizer is associated with
     */
    public AbstractOptimizer(Optimizable input, BoundType boundtype){
		this.bound = input;
		this.boundtype = boundtype;
	}

    /**
     * Minimizes the given bound w.r.t. to the maximum theta as well as theta and hoelder granularities.
     * 
     * @param thetagranularity
     * @param hoeldergranularity
     * 
     * @return A minimal value for the given bound
     * 
     * @throws ThetaOutOfBoundException
     * @throws ParameterMismatchException
     * @throws ServerOverloadException
     */
    @Override
    public abstract double minimize(double thetagranularity, double hoeldergranularity) throws ThetaOutOfBoundException, ParameterMismatchException, ServerOverloadException;
	
	/**
	 * Computes a bound on the violation probability that a 
	 * given backlog or delay is broken.
	 * 
	 * @param input the bound in arrival-representation
	 * @param boundtype either BACKLOG or DELAY
	 * @param bound the bound on the backlog or delay
	 * @param thetagranularity
	 * @param hoeldergranularity
	 * 
	 * @return a bound on the violation probability
	 * 
	 * @throws ParameterMismatchException 
	 * @throws ThetaOutOfBoundException 
	 * @throws ServerOverloadException
	 *  
	 * @deprecated Functionality is now covered by @link minimize
	 */
    @Deprecated
    @Override
	public abstract double Bound(Arrival input, BoundType boundtype, double bound, double thetagranularity, double hoeldergranularity) throws ThetaOutOfBoundException, ParameterMismatchException, ServerOverloadException;
	
	/**
	 * Computes the reverse bound for delay of backlog. The 
	 * reverse bound is the smallest possible backlog or delay, 
	 * such that a given violation probability is not broken.
	 *  
	 * @param input the bound in arrival-representation
	 * @param boundtype either BACKLOG or DELAY
	 * @param violation_probability the violation probability,
	 * @param thetagranularity
	 * @param hoeldergranularity
	 * 
	 * @return A value w.r.t. to the given violation probability
	 * 
	 * @throws ServerOverloadException 
	 * @throws ParameterMismatchException 
	 * @throws ThetaOutOfBoundException
	 * 
	 * @deprecated Functionality is now covered by @link minimize
	 */
    @Deprecated
    @Override
	public abstract double ReverseBound(Arrival input, BoundType boundtype, 
										double violation_probability, double thetagranularity, double hoeldergranularity) throws ThetaOutOfBoundException, ParameterMismatchException, ServerOverloadException;
}
