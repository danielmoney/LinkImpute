/*
 * This file is part of LinkImpute.
 * 
 * LinkImpute is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LinkImpute is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LinkImpute.  If not, see <http://www.gnu.org/licenses/>.
 */

package Methods;

import Exceptions.NotEnoughGenotypesException;
import Exceptions.WrongNumberOfSNPsException;
import Mask.Mask;
import Utils.Value;

/**
 * Wrapper around Knni to allow optimization of parameters
 * @author Daniel Money
 */
public class KnniOpt implements Value
{

    /**
     * Constructor
     * @param orig The original matrix
     * @param mask The mask
     * @param weight Distance (between samples) matrix
     */
    public KnniOpt(byte[][] orig, Mask mask, double[][] weight)
    {
        this(orig,mask,weight,false);
    }
    
    /**
     * Constructor
     * @param orig The original matrix
     * @param mask The mask
     * @param weight Distance (between samples) matrix
     * @param verbose Verbose output to standard out?
     */
    public KnniOpt(byte[][] orig, Mask mask, double[][] weight, boolean verbose)
    {
        this.orig = orig;
        this.mask = mask;
        this.verbose = verbose;
        if (verbose)
        {
            System.out.println("\tk\tAccuracy");
        }
        
        this.weight = weight;
    }
    
    @Override
    public double value(int[] k) throws NotEnoughGenotypesException,
           WrongNumberOfSNPsException
    {
        Knni knni = new Knni(k[0]);
        double v = knni.fastAccuracy(orig, mask, weight);
        if (verbose)
        {
            System.out.println("\t" + k[0] + "\t" + v);
        }
        return v;
    }
    
    private byte[][] orig;
    private Mask mask;
    private double[][] weight;
    private boolean verbose;
}
