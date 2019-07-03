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

package Correlation;

import Utils.Progress;
import Utils.SilentProgress;
import Utils.TextProgress;
import Utils.TopQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract class for calculating LD.  Calculating the LD between two SNPs is
 * left for implementing classes while this class deals with the all-against-all
 * mechanisms.
 * @author Daniel Money
 */
public abstract class Correlation
{

    /**
     * Default constructor
     */
    protected Correlation()
    {        
    }

    /**
     * Calculates all-against-all LD
     * @param data The data to calculate LD for.  SNPs are indexed by the first
     * position of the array, samples by the second.  For example data[1][2] would be
     * SNP 1 and sample 2.
     * @return A LD matrix
     */
    public double[][] calculate(byte[][] data)
    {
        double[][] result = new double[data.length][data.length];
        for (int i = 0; i < data.length; i++)
        {
            for (int j = i + 1; j < data.length; j++)
            {
                double v = calculate(data[i], data[j]);
                result[i][j] = v;
                result[j][i] = v;
            }
            result[i][i] = 0.0;
        }
        return result;
    }
    
    /**
     * Calculates all-against-all LD and returns the top n sites most in LD with
     * each site.
     * @param data The data to calculate LD for.  SNPs are indexed by the first
     * position of the array, samples by the second.  For example data[1][2] would be
     * SNP 1 and sample 2.
     * @param n number of top hits to return per site
     * @return A map from site to ordered list of sites most in LD
     */
    public Map<Integer,List<Integer>> topn(byte[][] data, int n)
    {
        Progress progress;
        if (SILENT)
        {
            progress = new SilentProgress();
        }
        else
        {
            long num = (long) data.length * ((long) data.length - 1) / 2;
            progress = new TextProgress(num);
        }
        Map<Integer,TopQueue<Integer,Double>> work = new HashMap<>();
        for (int i = 0; i < data.length; i++)
        {
            work.put(i,new TopQueue<Integer,Double>(n,true));
        }
        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        
        List<Single> parts = new ArrayList<>();
        for (int i = 0; i < data.length; i++)
        {
            parts.add(new Single(data,work,i,progress));
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
        Map<Integer,List<Integer>> result = new HashMap<>();
        for (Entry<Integer,TopQueue<Integer,Double>> e: work.entrySet())
        {
            result.put(e.getKey(),e.getValue().getList());
        }
        return result;
    }

    /**
     * Calculates the top n sites most in LD with a single site.
     * @param data The data to calculate LD for.  SNPs are indexed by the first
     * position of the array, samples by the second.  For example data[1][2] would be
     * SNP 1 and sample 2.
     * @param n Number of top hits to return
     * @param p Index of site to calculate other sites most in LD with it
     * @return Ordered list of sites most in LD
     */
    public List<Integer> topn(byte[][] data, int n, int p)
    {
        TopQueue<Integer,Double> tq = new TopQueue(n,true);
        for (int i = 0; i < data.length; i++)
        {
            if (i != p)
            {
                tq.add(i, calculate(data[i], data[p]));
            }
        }
        return tq.getList();
    }
    
    /**
     * Calculates LD between two SNPs
     * @param d1 SNP 1 genotype
     * @param d2 SNP 2 genotype
     * @return LD between the two SNPs
     */
    public abstract double calculate(byte[] d1, byte[] d2);
    
    private class Single implements Callable<Void>
    {
        public Single(byte[][] data, Map<Integer,TopQueue<Integer,Double>> work, int i, Progress progress)
        {
            this.data = data;
            this.work = work;
            this.i = i;
            this.progress = progress;
        }
        
        @Override
        public Void call()
        {
            for (int j = i + 1; j < data.length; j++)
            {
                double v = calculate(data[i], data[j]);
                work.get(i).add(j, v);
                work.get(j).add(i, v);
            }
            progress.done(data.length - i - 1);
            return null;
        }
        
        private final Progress progress;
        private final int i;
        private final byte[][] data;
        private final Map<Integer,TopQueue<Integer,Double>> work;
    }

    /**
     * Contols progress output to screen
     * @param s Whether to output progress to screen
     */
    public static void setSilent(boolean s)
    {
        SILENT = s;
    }
    
    private static boolean SILENT = false;
}
