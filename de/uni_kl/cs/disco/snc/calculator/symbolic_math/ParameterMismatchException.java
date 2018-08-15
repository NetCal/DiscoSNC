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
package de.uni_kl.cs.disco.snc.calculator.symbolic_math;

/**
 * Exception, which should be thrown, if the calculation of the
 * value of some {@link FunctionIF} fails, due to a mismatch of
 * parameters. For example calling {@link NewParameter.getValue} 
 * with a parameter-array of size 3 will result in this exception
 * been thrown. The reason is, that it only excepts parameter-arrays
 * of size 2.
 * 
 * @author Michael Beck
 * @see FunctionIF
 */
public class ParameterMismatchException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2278960541224825230L;

    /**
     *
     * @param s
     */
    public ParameterMismatchException(String s){
		super(s);
	}

}
