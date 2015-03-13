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
 * Class of static methods to calculate LD
 * @author Daniel Money
 */
public abstract class Correlation
{
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
    
    //public Map<Integer,Set<Integer>> topn(byte[][] data, int n)
    public Map<Integer,List<Integer>> topn(byte[][] data, int n)
    {
        Map<Integer,TopQueue<Integer,Double>> work = new HashMap<>();
        for (int i = 0; i < data.length; i++)
        {
            work.put(i,new TopQueue<Integer,Double>(n,true));
        }
        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        
        List<Single> parts = new ArrayList<>();
        for (int i = 0; i < data.length; i++)
        {
            parts.add(new Single(data,work,i));
            //es.submit(new Single(data,work,i));
            /*for (int j = i + 1; j < data.length; j++)
            {
                double v = calculate(data[i], data[j]);
                work.get(i).add(j, v);
                work.get(j).add(i, v);
            }*/
        }
        try
        {
            es.invokeAll(parts);
            es.shutdown();
            //es.awaitTermination(1, TimeUnit.HOURS);
        }
        catch (InterruptedException ex)
        {
            //NEED TO DEAL WITH THIS!
        }
        /*Map<Integer,Set<Integer>> result = new HashMap<>();
        for (Entry<Integer,TopQueue<Integer,Double>> e: work.entrySet())
        {
            result.put(e.getKey(),e.getValue().getSet());
        }*/
        Map<Integer,List<Integer>> result = new HashMap<>();
        for (Entry<Integer,TopQueue<Integer,Double>> e: work.entrySet())
        {
            result.put(e.getKey(),e.getValue().getList());
        }
        return result;
    }
    
    public abstract double calculate(byte[] d1, byte[] d2);
    
    private class Single implements Callable<Void>//Runnable
    {
        public Single(byte[][] data, Map<Integer,TopQueue<Integer,Double>> work, int i)
        {
            this.data = data;
            this.work = work;
            this.i = i;
        }
        
        //public void run()
        public Void call()
        {
            for (int j = i + 1; j < data.length; j++)
            {
                double v = calculate(data[i], data[j]);
                work.get(i).add(j, v);
                work.get(j).add(i, v);
            }
            if (((i + 1) % 100) == 0)
            {
                System.out.println("\tDone " + (i + 1));
            }
            return null;
        }
        
        private final int i;
        private final byte[][] data;
        private final Map<Integer,TopQueue<Integer,Double>> work;
    }
}
