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
import Utils.BufferByteArray2D;
import Utils.Progress;
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
     * Creates an object to perform LD-kNNi imputation with the default of k = 5 and l = 20
     * @param similar Similarity matrix between SNPs (normally a LD matrix).
     */
    /*public KnniLD(double[][] similar)
    {
        this(similar,5,20);
    }*/
    
    /**
     * Creates an object to perform LD-kNNi with the default of l = 20 and a given
     * value of k.
     * @param similar Similarity matrix between SNPs (normally a LD matrix).
     * @param k The value of k to be used
     */
    /*public KnniLD(double[][] similar, int k)
    {
        this(similar,k,20);
    }*/

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
    
    /*public KnniLD(Map<Integer,List<Integer>> topn)
    {
        this(topn,5,20);
    }
    
    public KnniLD(Map<Integer,List<Integer>> topn, int k)
    {
        this(topn,k,20);
    }*/
    
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
    /*public byte[][] compute(byte[][] original) throws NotEnoughGenotypesException, WrongNumberOfSNPsException
    {
        Progress progress = new Progress(original.length);
        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        byte[][] imputed = new byte[original.length][];

        // Loop over samples in order and then snps, imputing those genotypes that
        // are missing
        for (int s = 0; s < original.length; s++)
        {
            imputed[s] = new byte[original[s].length];
            List<Part> parts = new ArrayList<>();
            
            int preend = -1;
            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
            {
                int start = preend + 1;
                int end = (i+1) * original[s].length / Runtime.getRuntime().availableProcessors();
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
    }*/
    
    public byte[][] compute(byte[][] original) throws NotEnoughGenotypesException, WrongNumberOfSNPsException
    {
        return compute(new BufferByteArray2D(original));
    }
    
    public byte[][] compute(BufferByteArray2D ob) throws NotEnoughGenotypesException, WrongNumberOfSNPsException
    {
        
        //Progress progress = new Progress(original.length);
        Progress progress = new Progress(ob.outersize());
        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        
        //BufferByteArray2D ob = new BufferByteArray2D(original);

        //byte[][] imputed = new byte[original.length][];
        byte[][] imputed = new byte[ob.outersize()][];

        // Loop over samples in order and then snps, imputing those genotypes that
        // are missing
        //for (int s = 0; s < original.length; s++)
        for (int s = 0; s < ob.outersize(); s++)
        {
            //imputed[s] = new byte[original[s].length];
            imputed[s] = new byte[ob.innersize()];
            List<Part> parts = new ArrayList<>();
            
            int preend = -1;
            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
            {
                int start = preend + 1;
                int end = (i+1) * ob.innersize() / Runtime.getRuntime().availableProcessors();
                //int end = (i+1) * original[s].length / Runtime.getRuntime().availableProcessors();
                preend = end;
                
                parts.add(new Part(ob,imputed[s],s,start,end));
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
    
    /*private byte impute(int s, int p, byte[][] original) throws NotEnoughGenotypesException, WrongNumberOfSNPsException
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
    }*/
    
    private byte impute(int s, int p, BufferByteArray2D original) throws NotEnoughGenotypesException, WrongNumberOfSNPsException
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
            byte o = original.get(indicies[i],p);
            if (o >= 0)
            {
                w[o] += 1.0 / dist[indicies[i]];
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
    
    /*private double[] dist(int s, int p, byte[][] values) throws WrongNumberOfSNPsException
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
    }*/
    
    private double[] dist(int s, int p, BufferByteArray2D values) throws WrongNumberOfSNPsException
    {
        //Simply loops round the other samples, catching the case where it's
        //the current sample
        double[] ret = new double[values.outersize()];
        for (int i = 0; i < values.outersize(); i++)
        {
            if (i != s)
            //if ((i != s) && (values[i].get(p) >= 0))
            {
                ret[i] = sdist(values, s, i, p);
            }
            else
            {
                //ret[i] = 0.0;
                ret[i] = Double.MAX_VALUE;
            }
        }
        return ret;
    }
    
    /*private double sdist(byte[] v1, byte[] v2, int p) throws WrongNumberOfSNPsException
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
    }*/
    
    private double sdist(BufferByteArray2D values, int v1, int v2, int p) throws WrongNumberOfSNPsException
    {
        int d = 0;
        int c = 0;
        // Get the most similar snps to the current snp
        Integer[] s = sim[p];
        // Use the l most similar ones to calculate the distance
        for (int j = 0; j < l; j++)
        {
            int i = s[j];
            int p1 = values.get(v1, i);
            int p2 = values.get(v2, i);
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
    
    /*public double fastAccuracy(byte[][] original, Mask mask)
    {
        boolean[][] maskA = mask.getArray();
        //List<SampleSnp> fullList = mask.getList();
        
        int nt = Runtime.getRuntime().availableProcessors();
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
                    lists.get(ct).add(new SampleSnp(i,j,original[i][j]));
                    ct = (ct + 1) % nt;
                    cm ++;
                }
            }
        }
        
        //for (SampleSnp s: fullList)
        //{
        //    lists.get(ct).add(s);
        //    ct = (ct + 1) % nt;
        //}
        
        List<FastPart> parts = new ArrayList<>();
        for (List<SampleSnp> l: lists)
        {
            parts.add(new FastPart(original,l));
        }
        
        ExecutorService es = Executors.newFixedThreadPool(nt);
        
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
        //return (double) cc / (double) fullList.size();
    }*/
    
    public double fastAccuracy(byte[][] original, Mask mask)
    {
        return fastAccuracy(new BufferByteArray2D(original),mask);
    }
    
    public double fastAccuracy(BufferByteArray2D ob, Mask mask)
    {
        //boolean[][] maskA = mask.getArray();
        //List<SampleSnp> fullList = mask.getList();
        
        int nt = Runtime.getRuntime().availableProcessors();
        List<List<SampleSnp>> lists = new ArrayList<>(nt);
        for (int t = 0; t < nt; t++)
        {
            lists.add(new ArrayList<SampleSnp>());
        }
        int ct = 0;
        int cm = 0;
        /*for (int i = 0; i < maskA.length; i++)
        {
            boolean[] m = maskA[i];
            for (int j = 0; j < m.length; j++)
            {
                if (m[j])
                {
                    lists.get(ct).add(new SampleSnp(i,j));//,ob.get(i, j)));
                    ct = (ct + 1) % nt;
                    cm ++;
                }
            }
        }*/
        for (SampleSnp ss: mask.getList())
        {
                    lists.get(ct).add(ss);//,ob.get(i, j)));
                    ct = (ct + 1) % nt;
                    //cm ++;            
        }
        
        //for (SampleSnp s: fullList)
        //{
        //    lists.get(ct).add(s);
        //    ct = (ct + 1) % nt;
        //}
        
        List<FastPart> parts = new ArrayList<>();
        for (List<SampleSnp> l: lists)
        {
            parts.add(new FastPart(ob,l));
        }
        
        ExecutorService es = Executors.newFixedThreadPool(nt);
        
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
        
        //return (double) cc / (double) cm;
        return (double) cc / (double) mask.getList().size();
        //return (double) cc / (double) fullList.size();
    }
    
    /*private class Part implements Callable<Void>
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
    }*/
    
    private class Part implements Callable<Void>
    {
        public Part(BufferByteArray2D original, byte[] imputed,
                int s, int start, int end)
        {
            this.original = original;
            this.imputed = imputed;
            this.s = s;
            this.start = start;
            this.end = end;
        }
        
        public Void call() throws NotEnoughGenotypesException, WrongNumberOfSNPsException
        {
            for (int p = start; p < end; p++)
            {
                byte o = original.get(s,p);
                if (o >= 0)
                {
                    imputed[p] = o;
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
        private final BufferByteArray2D original;
    }
    
    /*private class FastPart implements Callable<Integer>
    {
        //public Part(byte[][] original, byte[] imputed, BufferByteArray[] o,
        public FastPart(byte[][] orig, List<SampleSnp> todo)
        {
            this.orig = orig;
            this.todo = todo;
        }
        
        public Integer call() throws NotEnoughGenotypesException, WrongNumberOfSNPsException
        {
            int c = 0;
            for (SampleSnp ss: todo)
            {                
                byte imp = impute(ss.getSample(), ss.getSnp(), orig);
                if (imp == ss.getOriginal())
                {
                    c++;
                }
            }
            return c;
        }
        
        private final List<SampleSnp> todo;
        private final byte[][] orig;
    }*/
    
    private class FastPart implements Callable<Integer>
    {
        public FastPart(BufferByteArray2D orig, List<SampleSnp> todo)
        {
            this.orig = orig;
            this.todo = todo;
        }
        
        public Integer call() throws NotEnoughGenotypesException, WrongNumberOfSNPsException
        {
            int c = 0;
            for (SampleSnp ss: todo)
            {                
                byte imp = impute(ss.getSample(), ss.getSnp(), orig);
                //if (imp == ss.getOriginal())
                if (imp == orig.get(ss.getSample(), ss.getSnp()))
                {
                    c++;
                }
            }
            return c;
        }
        
        private final List<SampleSnp> todo;
        private final BufferByteArray2D orig;
    }
    
    Integer[] samporder;
    Integer[][] sim;
    private int k;
    private int l;
}
