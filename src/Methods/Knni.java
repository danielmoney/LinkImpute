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
import Utils.BufferByteArray;
import Utils.SortByIndexDouble;

/**
 * Class to perform standard kNNi imputation
 * @author Daniel Money
 */
public class Knni
{

    /**
     * Creates an object to perform standard kNNi imputation with the default of k = 5
     */
    public Knni()
    {
        this(5);
    }
    
    /**
     * Creates an object to perform standard kNNi with a given value of k
     * @param k The value of k to be used
     */
    public Knni(int k)
    {
        this.k = k;
    }
    
    /**
     * Impute missing data
     * @param original The original data set.  Missing data is coded as -1
     * @throws NotEnoughGenotypesException If there is not enough known genotypes for
     * imputation
     * @throws WrongNumberOfSNPsException If the number of SNPs is not the same for
     * every sample
     * @return The imputed data set.
     */
    public byte[][] compute(byte[][] original) throws NotEnoughGenotypesException, WrongNumberOfSNPsException
    {
        // Convert the data to BufferByteArrays for calculating the weights - see
        // BufferByteAray for more on this
        BufferByteArray[] o = new BufferByteArray[original.length];
        for (int i = 0; i < original.length; i++)
        {
            o[i] = new BufferByteArray(original[i]);
        }
        
        return compute(original, weight(o));
    }
    
    // Written as a seperate function to allow possible future expansion with
    // a different distance function
    private byte[][] compute(byte[][] original, double[][] d) throws NotEnoughGenotypesException, WrongNumberOfSNPsException
    {
        // Comment out code to catch a possible error as this can only happen with
        // a user passed distance matrix and at the moment this function is private
        //if (d.length != original.length)
        //{
        //    System.err.println("This should be an error.  Things are about to go worng!");
        //}

        byte[][] imputed = new byte[original.length][];
        
        for (int s = 0; s < original.length; s++)
        {
            // Get a list of indicies to other samples in order from closest to
            // furthest from the current sample
            SortByIndexDouble si = new SortByIndexDouble(d[s],true);
            Integer[] indicies = si.sort();
            
            // Loop over snps.  If the genotypes is known simply copy to new array,
            // else impute
            imputed[s] = new byte[original[s].length];
            for (int p = 0; p < original[s].length; p++)
            {
                if (original[s][p] >= 0)
                {
                    imputed[s][p] = original[s][p];
                }
                else
                {
                    imputed[s][p] = impute(p, original, indicies, d[s]);
                }
            }
        }
       
        return imputed;
    }
    
    private byte impute(int p, byte[][] original, Integer[] indicies, double[] dist) throws NotEnoughGenotypesException
    {
        int f = 0;
        int i = 0;
        
        // Store the weights applicable to each of the three genotypes
        double[] w = new double[3];
        //Loop around samples in order of distance
        do
        {
            // Only impute from samples that have a genotype for the crrent SNP
            if (original[indicies[i]][p] >= 0)
            {
                // If we have a sample at a distance of zero simply impute from that
                if (dist[indicies[i]] == Double.POSITIVE_INFINITY)
                {
                    return original[indicies[i]][p];
                }
                w[original[indicies[i]][p]] += dist[indicies[i]];
                f++;
            }
            i++;
        }
        // While we haven't seen enough known genotypes and there's still samples left
        while ((f < k) && (i < indicies.length));
        
        if (f < k)
        {
            //Throw an error - we don't have k samples with values to impute from
            throw new NotEnoughGenotypesException(p,k);
        }
        
        // Return the genotype with most weight
        if ((w[0] >= w[1]) && (w[0] >= w[2]))
        {
            return 0;
        }
        if (w[1] >= w[2])
        {
            return 1;
        }
        return 2;
    }
    
    private double[][] weight(BufferByteArray[] values) throws WrongNumberOfSNPsException
    {
        //Calculate the weights for every pair of samples
        double[][] ret = new double[values.length][values.length];
        for (int i = 0; i < values.length; i++)
        {
            for (int j = i + 1; j < values.length; j++)
            {
                double sd = sweight(values[i], values[j]);
                ret[i][j] = sd;
                ret[j][i] = sd;
            }
            ret[i][i] = 0.0;
        }
        return ret;
    }
    
    private double sweight(BufferByteArray v1, BufferByteArray v2) throws WrongNumberOfSNPsException
    {
        //Calculate the weight for a single pair of samples
        if (v1.size() == v2.size())
        {
            int d = 0;
            int c = 0;
            for (int i = 0; i < v1.size(); i++)
            {
                int p1 = v1.get(i);
                int p2 = v2.get(i);                
                if ((p1 != -1) && (p2 != -1))
                {
                    // Count how many snps we use in the calculation for weighting
                    // purposes
                    c++;
                    // Caclulate the taxicab distance
                    d += Math.abs(p1 - p2); 
                
                }
            }
            // Return a scaled weight
            return 1.0 / ((double) d * (double) v1.size() / (double) c);
        }
        else
        {
            //Shouldn't reach here if you use one of the provided data reading
            //methods as they already test for this.  Hence why the error is
            //currently not too informative
            throw new WrongNumberOfSNPsException("Unknown");
        }        
    }
    
    // How many neighbours to use
    private int k;
}
