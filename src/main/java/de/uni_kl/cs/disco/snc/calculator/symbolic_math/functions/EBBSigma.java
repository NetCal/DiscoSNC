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

package de.uni_kl.cs.disco.snc.calculator.symbolic_math.functions;

import java.util.HashMap;
import java.util.Map;

import de.uni_kl.cs.disco.snc.calculator.symbolic_math.BadInitializationException;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.Hoelder;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.ParameterMismatchException;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.ServerOverloadException;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.SymbolicFunction;
import de.uni_kl.cs.disco.snc.calculator.symbolic_math.ThetaOutOfBoundException;

/**
 * A class representing the sigma-term in MGF-bounds derived
 * from an EBB traffic model. In that class, if t is the input-
 * parameter theta the resulting funciton is:<br>
 * EBB(t) = log((1-t/decay)^(-1/t) * prefactor^(decay-1/t))<br>
 * As prefactor and decay are input parameters of the traffic
 * model that do not change during an analysis of the system, 
 * they are not considered as functions themselves
 * 
 * Since the conversion theorem is applied to an EBB traffic
 * decription the maximal range of theta is capped by the para-
 * meter <code>decay</code>. 
 * 
 * @author Michael Beck
 * 
 * @see FunctionIF
 * @see ArrivalFactory
 */
public class EBBSigma implements SymbolicFunction {
	private static final long serialVersionUID = 3048571928799189089L;
	
	private double decay;
	private double prefactor;
	
	public EBBSigma(double decay, double prefactor) throws BadInitializationException{
		if(decay < 0){
			throw new BadInitializationException("Decay must be larger zero.", decay);
		}
		if(prefactor < 0){
			throw new BadInitializationException("Prefactor must be larger zero.", prefactor);
		}
		this.decay = decay;
		this.prefactor = prefactor;
	}	

	/**
	 * Calculates the value of the resulting EBB-function at theta 
	 * with given <code>parameters</code>.
	 * 
	 * @param parameters contains the needed parameters (none are needed
	 * as there are no atom-functions involved).
	 * 
	 * @return the value of the EBB-function
	 * 
	 * @throws ParameterMismatchException 
	 */
	@Override
	public double getValue(double theta, Map<Integer, Hoelder> parameters)
			throws ThetaOutOfBoundException, ServerOverloadException, ParameterMismatchException {

		// Checks for a mismatch of given and needed parameters
		if(parameters.size() != 0) throw new ParameterMismatchException("EBBFunction has only (modified) theta as parameters.");
		
		// Checks if theta is larger the decay-rate (in which case the integral appearing in the conversion theorem is indefinite)
		if(theta > decay){
			throw new ThetaOutOfBoundException("The given theta exceeds the decay-rate of this EBB-arrival. theta: "+theta+". decay-rate: "+decay);
		} else {
			return 1/decay*Math.log(prefactor) - 1/theta*Math.log(1 - theta/decay);
		}
	}

	/**
	 * Returns a string representation of the EBB-function. In the 
	 * form <code>EBB(rate,decay,prefactor)</code>.
	 */
	@Override
	public String toString(){
		StringBuffer ebb_sigma_str = new StringBuffer();
		
		ebb_sigma_str.append("EBB(");
		ebb_sigma_str.append(Double.toString(decay));
		ebb_sigma_str.append(",");
		ebb_sigma_str.append(Double.toString(prefactor));
		ebb_sigma_str.append(")");
		
		return ebb_sigma_str.toString();
	}
	
	@Override
	public double getmaxTheta() {
		return decay;
	}

	@Override
	public Map<Integer, Hoelder> getParameters() {
		return new HashMap<Integer, Hoelder>(0);
	}
}
