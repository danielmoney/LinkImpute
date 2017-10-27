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
import Utils.SortByIndexDouble;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        return compute(original, weight(original));
    }
    
    // Written as a seperate function so distances only have to be calculated
    // once if multiple Knni instances (with different values of k) are being
    // used.
    /**
     * Impute missing data
     * @param original The original data set.  Missing data is coded as -1
     * @param d Distance matrix giving the distance between samples
     * @throws NotEnoughGenotypesException If there is not enough known genotypes for
     * imputation
     * @throws WrongNumberOfSNPsException If the number of SNPs is not the same for
     * every sample
     * @return The imputed data set.
     */
    public byte[][] compute(byte[][] original, double[][] d) throws NotEnoughGenotypesException, WrongNumberOfSNPsException
    {
        // NEED SOME PROPER ERROR CHECKING HERE, IN CASE THE NUMBER OF SAMPLES IN
        // ORIGINAL AND D DISAGREE

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
    
    /**
     * Performs a fast accuracy calculation - only imputes those genotypes that
     * were masked rather than all missing genotypes.
     * @param original The original genotype values
     * @param mask A mask
     * @return The percentage of genotypes imputed correctly
     * @throws Exceptions.NotEnoughGenotypesException Thrown if there are not k
     * known genotypes avaliable for a SNP
     */  
    public double fastAccuracy(byte[][] original, Mask mask) throws NotEnoughGenotypesException
    {
        return fastAccuracy(original, mask, weight(original));
    }

    /**
     * Performs a fast accuracy calculation - only imputes those genotypes that
     * were masked rather than all missing genotypes.
     * @param original The original genotype values
     * @param mask A mask
     * @param d Distance matrix giving the distance between samples
     * @return The percentage of genotypes imputed correctly
     * @throws Exceptions.NotEnoughGenotypesException Thrown if there are not k
     * known genotypes avaliable for a SNP
     */  
    public double fastAccuracy(byte[][] original, Mask mask, double[][] d) throws NotEnoughGenotypesException
    {
        boolean[][] maskA = mask.getArray();
        int correct = 0;
        int total = 0;        
        
        for (int i = 0; i < maskA.length; i++)
        {
            boolean[] m = maskA[i];
            for (int j = 0; j < m.length; j++)
            {
                if (m[j])
                {
                    SortByIndexDouble si = new SortByIndexDouble(d[i],true);
                    Integer[] indicies = si.sort();
                    
                    byte imputed = impute(j ,original, indicies, d[i]);
                    if (imputed == original[i][j])
                    {
                        correct++;
                    }
                    total++;
                }
            }
        }
        
        return (double) correct / (double) total;
    }
    
    /**
     * Calculates distances between samples using a scaled taxicab distances
     * @param values Genotype array
     * @return Distance (between samples) array
     */
    public static double[][] weight(byte[][] values)
    {
        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        double[][] result = new double[values.length][values[0].length];
        
        List<Single> parts = new ArrayList<>();
        for (int i = 0; i < values.length; i++)
        {
            parts.add(new Single(values,result,i));
        }
        try
        {
            es.invokeAll(parts);
            es.shutdown();
        }
        catch (InterruptedException ex)
        {
            //NEED TO DEAL WITH THIS!
        }
        return result;
    }
    
    private static double sweight(byte[] v1, byte[] v2) throws WrongNumberOfSNPsException
    {
        //Calculate the weight for a single pair of samples
        if (v1.length == v2.length)
        {
            int d = 0;
            int c = 0;
            for (int i = 0; i < v1.length; i++)
            {
                int p1 = v1[i];
                int p2 = v2[i];                
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
            return 1.0 / ((double) d * (double) v1.length / (double) c);
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
    
    private static class Single implements Callable<Void>//Runnable
    {
        public Single(byte[][] data, double[][] res, int i)
        {
            this.data = data;
            this.res = res;
            this.i = i;
        }
        
        @Override
        public Void call()
        {
            for (int j = i + 1; j < data.length; j++)
            {
                try
                {
                    double v = sweight(data[i], data[j]);
                    res[i][j] = v;
                    res[j][i] = v;
                }
                catch (WrongNumberOfSNPsException ex)
                {
                    //Should never get here!
                }

            }
            return null;
        }
        
        private final int i;
        private final byte[][] data;
        private final double[][] res;
    }
}
