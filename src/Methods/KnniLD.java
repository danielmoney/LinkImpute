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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class to perform standard LD-kNNi imputation
 * @author Daniel Money
 */
public class KnniLD
{

    /**
     * Creates an object to perform LD-kNNi imputation with the default of k = 5 and l = 20
     * @param similar Similarity matrix between SNPs (normally a LD matrix).
     */
    public KnniLD(double[][] similar)
    {
        this(similar,5,20);
    }
    
    /**
     * Creates an object to perform LD-kNNi with the default of l = 20 and a given
     * value of k.
     * @param similar Similarity matrix between SNPs (normally a LD matrix).
     * @param k The value of k to be used
     */
    public KnniLD(double[][] similar, int k)
    {
        this(similar,k,20);
    }

    /**
     * Creates an object to perform LD-kNNi with given values of k and l.
     * @param similar Similarity matrix between SNPs (normally a LD matrix).
     * @param k The value of k to be used
     * @param l The value of l to be used
     */
    public KnniLD(double[][] similar, int k, int l)
    {
        this.k = k;
        this.l = l;
        sim = new Integer[similar.length][];
        //For each snp get a ranked list, by similarity, of the other snps
        for (int i = 0; i < similar.length; i++)
        {
            SortByIndexDouble si = new SortByIndexDouble(similar[i],true);
            sim[i] = si.sort();
        }
    }
    
    //public KnniLD(Map<Integer,Set<Integer>> topn)
    public KnniLD(Map<Integer,List<Integer>> topn)
    {
        this(topn,5,20);
    }
    
    //public KnniLD(Map<Integer,Set<Integer>> topn, int k)
    public KnniLD(Map<Integer,List<Integer>> topn, int k)
    {
        this(topn,k,20);
    }
    
    //public KnniLD(Map<Integer,Set<Integer>> topn, int k, int l)
    public KnniLD(Map<Integer,List<Integer>> topn, int k, int l)
    {
        this.k = k;
        this.l = l;
        sim = new Integer[topn.size()][];
        //for (Entry<Integer,Set<Integer>> e: topn.entrySet())
        for (Entry<Integer,List<Integer>> e: topn.entrySet())
        {
            Integer[] a = new Integer[e.getValue().size()];
            sim[e.getKey()] = e.getValue().toArray(a);
        }
    }
    
    /**
     * Impute missing data
     * @param original The original data set.  Missing data is coded as -1
     * @return The imputed data set
     * @throws NotEnoughGenotypesException If there is not enough known genotypes for
     * @throws WrongNumberOfSNPsException If the number of SNPs is not the same for
     * every sample
     * imputation
     */
    public byte[][] compute(byte[][] original) throws NotEnoughGenotypesException, WrongNumberOfSNPsException
    {
        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        //ExecutorService es = Executors.newFixedThreadPool(1);
        //Calculate the amount of missingness for each sample
        double[] miss = new double[original.length];
        for (int i = 0; i < original.length; i++)
        {
            byte[] o = original[i];
            int c = 0;
            for (int j = 0; j < o.length; j++)
            {
                if (o[j] == -1)
                {
                    c++;
                }
            }
            miss[i] = (double) c / (double) o.length;
        }
        
        // Sort indexes by missingness (least to most) - this is the order we
        // will impute in
        SortByIndexDouble si = new SortByIndexDouble(miss);
        samporder = si.sort();
        
        // Create BufferByteArrays - see that class for reason behind this.
        BufferByteArray[] o = new BufferByteArray[original.length];
        for (int i = 0; i < original.length; i++)
        {
            o[i] = new BufferByteArray(original[i]);
        }
        
        byte[][] imputed = new byte[original.length][];

        int c = 0;
        // Loop over samples in order and then snps, imputing those genotypes that
        // are missing
        for (int s: samporder)
        {
            imputed[s] = new byte[original[s].length];
            //Added
            BufferByteArray newo = o[s].clone();
            List<Part> parts = new ArrayList<>();
            
            int preend = -1;
            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
            {
                int start = preend + 1;
                int end = (i+1) * original[s].length / Runtime.getRuntime().availableProcessors();
                preend = end;
                
                //parts.add(new Part(original,imputed[s],o,s,start,end));
                parts.add(new Part(original,imputed[s],o,newo,s,start,end));
            }
            try
            {
                es.invokeAll(parts);
            }
            catch (InterruptedException ex)
            {
                //NEED TO DEAL WITH THIS PROPERLY!
            }
            o[s] = newo;
            c++;
            if ((c % 100) == 0)
            {
                System.out.println("\tDone 1:" + c);
            }
            
            /*for (int p = 0; p < original[s].length; p++)
            {
                if (original[s][p] >= 0)
                {
                    imputed[s][p] = original[s][p];
                }
                else
                {
                    byte imp = impute(s, p, o);
                    imputed[s][p] = imp;
                    o[s].set(p, imp);
                }
            }*/
        }
        
        // Create a second lot of BufferedByteArrays for the second round of imputation
        BufferByteArray[] o2 = new BufferByteArray[imputed.length];
        for (int i = 0; i < imputed.length; i++)
        {
            o2[i] = new BufferByteArray(imputed[i]);
        }
        
        byte[][] imputed2 = new byte[original.length][];

        c = 0;
        // For genotypes that were originally missing impute again, this time
        // starting with the complete genotype matrix from the first round
        for (int s: samporder)
        {
            imputed2[s] = new byte[original[s].length];
            List<Part> parts = new ArrayList<>();
            //Added
            BufferByteArray newo = o2[s].clone();
            
            int preend = -1;
            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
            {
                int start = preend + 1;
                int end = (i+1) * original[s].length / Runtime.getRuntime().availableProcessors();
                preend = end;
                
                parts.add(new Part(original,imputed2[s],o2,newo,s,start,end));
            }
            try
            {
                es.invokeAll(parts);
            }
            catch (InterruptedException ex)
            {
                //NEED TO DEAL WITH THIS PROPERLY!
            }
            o2[s] = newo;
            c++;
            if ((c % 100) == 0)
            {
                System.out.println("\tDone 2:" + c);
            }
            
            /*imputed2[s] = new byte[original[s].length];
            for (int p = 0; p < original[s].length; p++)
            {
                if (original[s][p] >= 0)
                {
                    imputed2[s][p] = original[s][p];
                }
                else
                {
                    byte imp = impute(s, p, o2);
                    imputed2[s][p] = imp;
                    o2[s].set(p, imp);
                }
            }*/
        }
        
        es.shutdown();
       
        return imputed2;
    }
    
    private byte impute(int s, int p, BufferByteArray[] original) throws NotEnoughGenotypesException, WrongNumberOfSNPsException
    {
        //Calculate the distance to other samples for this snp / sample combination
        double[] dist = dist(s,p,original);
        
        //Order the samples by their distance
        SortByIndexDouble si = new SortByIndexDouble(dist);
        Integer[] indicies = si.sort();
        
        int f = 0;
        //int i = 1;
        int i = 0;

        // Store the weights applicable to each of the three genotypes
        double[] w = new double[3];
        //Loop around samples in order of distance
        do
        {
            // Only impute from samples that have a genotype for the crrent SNP
            if (original[indicies[i]].get(p) >= 0)
            {
                // If we have a sample at a distance of zero simply impute from that
                //TEMP if (dist[indicies[i]] == 0.0)
                //{
                //    return original[indicies[i]].get(p);
                //}  
                w[original[indicies[i]].get(p)] += 1.0 / dist[indicies[i]];
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
    
    //Calclate the distance to other samples based on the current sample and snp
    private double[] dist(int s, int p, BufferByteArray[] values) throws WrongNumberOfSNPsException
    {
        //Simply loops round the other samples, catching the case where it's
        //the current sample
        double[] ret = new double[values.length];
        for (int i = 0; i < values.length; i++)
        {
            if (i != s)
            //if ((i != s) && (values[i].get(p) >= 0))
            {
                ret[i] = sdist(values[s], values[i], p);
            }
            else
            {
                //ret[i] = 0.0;
                ret[i] = Double.MAX_VALUE;
            }
        }
        return ret;
    }
    
    private double sdist(BufferByteArray v1, BufferByteArray v2, int p) throws WrongNumberOfSNPsException
    {
        if (v1.size() == v2.size())
        {
            int d = 0;
            int c = 0;
            // Get the most similar snps to the current snp
            Integer[] s = sim[p];
            // Use the l most similar ones to calculate the distance
            for (int j = 0; j < l; j++)
            {
                int i = s[j];
                int p1 = v1.get(i);
                int p2 = v2.get(i);
                if ((p1 != -1) && (p2 != -1))
                {
                    // c counts how many snps we've actually used to scale the
                    // distance with since some snps will be unknown
                    c++;
                    d += Math.abs(p1 - p2);
                }
            }
            // If across the l most similar snps there wasn't a single case
            // where both samples had a known genotype then set the distance to
            // max
            //HO-HUM, THE FOLLOWING LINE MAKES THINGS BETTER!
            //DOING THE +1 BELOW MAKES MORE SENSE TO ME
            //d = d + 1;
            if (c == 0)
            {
                return Double.MAX_VALUE;
            }
            //Else return the scaled distance
            else
            {
                return ((double) d * (double) l / (double) c) + 1.0;
            }
        }
        else
        {
            //Shouldn't reach here if you use one of the provided data reading
            //methods as they already test for this.  Hence why the error is
            //currently not too informative
            throw new WrongNumberOfSNPsException("Unknown");
        }        
    }
    
    private class Part implements Callable<Void>
    {
        //public Part(byte[][] original, byte[] imputed, BufferByteArray[] o,
        public Part(byte[][] original, byte[] imputed, BufferByteArray[] o, BufferByteArray newo,
                int s, int start, int end)
        {
            this.original = original;
            this.imputed = imputed;
            this.o = o;
            this.newo = newo;
            this.s = s;
            this.start = start;
            this.end = end;
        }
        
        public Void call() throws NotEnoughGenotypesException, WrongNumberOfSNPsException
        {
            for (int p = start; p < end; p++)
            {
                if (original[s][p] >= 0)
                {
                    imputed[p] = original[s][p];
                }
                else
                {
                    byte imp = impute(s, p, o);
                    imputed[p] = imp;
                    newo.set(p, imp);
                }
            }
            return null;
        }
        
        private final int s;
        private final int start;
        private final int end;
        private final byte[] imputed;
        private final byte[][] original;
        private final BufferByteArray[] o;
        private final BufferByteArray newo;
        
    }
    
    Integer[] samporder;
    Integer[][] sim;
    private int k;
    private int l;
}
