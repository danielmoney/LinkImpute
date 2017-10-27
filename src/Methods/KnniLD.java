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
import Mask.SampleSnp;
import Utils.Progress;
import Utils.SilentProgress;
import Utils.TextProgress;
import Utils.SortByIndexDouble;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class to perform standard LD-kNNi imputation
 * @author Daniel Money
 */
public class KnniLD
{
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
    
    /**
     * Creates an object to perform LD-kNNi with given values of k and l.
     * @param topn A map from SNP to a list of most similar SNPs
     * @param k The value of k to be used
     * @param l The value of l to be used
     */
    public KnniLD(Map<Integer,List<Integer>> topn, int k, int l)
    {
        this.k = k;
        this.l = l;
        sim = new Integer[topn.size()][];
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
        int nt = Runtime.getRuntime().availableProcessors();
        ExecutorService es = Executors.newFixedThreadPool(nt);

        
        Progress progress;
        if (SILENT)
        {
            progress = new SilentProgress();
        }
        else
        {
            progress = new TextProgress(original.length);
        }

        byte[][] imputed = new byte[original.length][];

        // Loop over samples in order and then snps, imputing those genotypes that
        // are missing
        for (int s = 0; s < original.length; s++)
        {
            imputed[s] = new byte[original[s].length];
            List<Part> parts = new ArrayList<>();
            
            int preend = 0;
            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
            {
                int start = preend;
                int end = (i+1) * original[s].length / Runtime.getRuntime().availableProcessors() + 1;
                preend = end;
                
                parts.add(new Part(original,imputed[s],s,start,end));
            }
            try
            {
                es.invokeAll(parts);
            }
            catch (InterruptedException ex)
            {
                //NEED TO DEAL WITH THIS PROPERLY!
            }
            progress.done();
        }
        
        es.shutdown();
        return imputed;
    }
    
    private byte impute(int s, int p, byte[][] original) throws NotEnoughGenotypesException, WrongNumberOfSNPsException
    {
        //Calculate the distance to other samples for this snp / sample combination
        double[] dist = dist(s,p,original);
        
        //Order the samples by their distance
        SortByIndexDouble si = new SortByIndexDouble(dist);
        Integer[] indicies = si.sort();
        
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
                w[original[indicies[i]][p]] += 1.0 / dist[indicies[i]];
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
    
    private double[] dist(int s, int p, byte[][] values) throws WrongNumberOfSNPsException
    {
        //Simply loops round the other samples, catching the case where it's
        //the current sample
        double[] ret = new double[values.length];
        for (int i = 0; i < values.length; i++)
        {
            if (i != s)
            {
                ret[i] = sdist(values[s], values[i], p);
            }
            else
            {
                ret[i] = Double.MAX_VALUE;
            }
        }
        return ret;
    }
    
    private double sdist(byte[] v1, byte[] v2, int p) throws WrongNumberOfSNPsException
    {
        if (v1.length == v2.length)
        {
            int d = 0;
            int c = 0;
            // Get the most similar snps to the current snp
            Integer[] s = sim[p];
            // Use the l most similar ones to calculate the distance
            for (int j = 0; j < l; j++)
            {
                int i = s[j];
                int p1 = v1[i];
                int p2 = v2[i];
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
            if (c == 0)
            {
                return Double.MAX_VALUE;
            }
            //Else return the scaled distance (adding a constant so we don't have a
            //distance of zero as that caused problems later.
            else
            {
                return ((double) d * (double) l / (double) c) + ADD_CONSTANT;
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
    
    /**
     * Performs a fast accuracy calculation - only imputes those genotypes that
     * were masked rather than all missing genotypes.
     * @param original The original genotype values
     * @param mask A mask
     * @return The percentage of genotypes imputed correctly
     */    
    public double fastAccuracy(byte[][] original, Mask mask)
    {
        int nt = Runtime.getRuntime().availableProcessors();
        ExecutorService es = Executors.newFixedThreadPool(nt);        
    
        boolean[][] maskA = mask.getArray();
        //List<SampleSnp> fullList = mask.getList();
        
        //int nt = Runtime.getRuntime().availableProcessors();
        List<List<SampleSnp>> lists = new ArrayList<>(nt);
        for (int t = 0; t < nt; t++)
        {
            lists.add(new ArrayList<SampleSnp>());
        }
        int ct = 0;
        int cm = 0;
        for (int i = 0; i < maskA.length; i++)
        {
            boolean[] m = maskA[i];
            for (int j = 0; j < m.length; j++)
            {
                if (m[j])
                {
                    lists.get(ct).add(new SampleSnp(i,j));
                    ct = (ct + 1) % nt;
                    cm ++;
                }
            }
        }
        
        List<FastPart> parts = new ArrayList<>();
        for (List<SampleSnp> list: lists)
        {
            parts.add(new FastPart(original,list));
        }
        
        int cc = 0;
        try
        {
            List<Future<Integer>> results = es.invokeAll(parts);

            for (Future<Integer> f: results)
            {
                cc += f.get();
            }
        }
        catch (InterruptedException | ExecutionException ex)
        {
            //NEED TO DEAL WITH THIS PROPERLY
        }
        
        es.shutdown();
        return (double) cc / (double) cm;
    }
    
    private class Part implements Callable<Void>
    {
        public Part(byte[][] original, byte[] imputed,
                int s, int start, int end)
        {
            this.original = original;
            this.imputed = imputed;
            this.s = s;
            this.start = start;
            this.end = end;
        }
        
        @Override
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
                    byte imp = impute(s,p,original);
                    imputed[p] = imp;
                }
            }
            return null;
        }
        
        private final int s;
        private final int start;
        private final int end;
        private final byte[] imputed;
        private final byte[][] original;
    }
    
    private class FastPart implements Callable<Integer>
    {
        public FastPart(byte[][] orig, List<SampleSnp> todo)
        {
            this.orig = orig;
            this.todo = todo;
        }
        
        @Override
        public Integer call() throws NotEnoughGenotypesException, WrongNumberOfSNPsException
        {
            int c = 0;
            for (SampleSnp ss: todo)
            {                
                byte imp = impute(ss.getSample(), ss.getSnp(), orig);
                if (imp == orig[ss.getSample()][ss.getSnp()])
                {
                    c++;
                }
            }
            return c;
        }
        
        private final List<SampleSnp> todo;
        private final byte[][] orig;
    }
    
    public static void setAddConstant(double constant)
    {
        ADD_CONSTANT = constant;
    }
    
    public static void setSilent(boolean s)
    {
        SILENT = s;
    }
    
    Integer[][] sim;
    private final int k;
    private final int l;
    
    private static double ADD_CONSTANT = 1.0;
    private static boolean SILENT = false;
}
