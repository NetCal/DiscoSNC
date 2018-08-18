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

/**
 * This exception is thrown by the {@link Analysis} class if the
 * stack of vertices, which can serve their flows runs empty, but 
 * the FoI or SoI is not found yet.
 * 
 * @author Michael Beck
 * 
 * @see Analysis
 */
public class DeadlockException extends Exception {

	private static final long serialVersionUID = 1955595315940127710L;

    public DeadlockException(String e){
		super(e);
	}
}
