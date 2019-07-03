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
package Similarity;

import Correlation.Correlation;

/**
 * Calculates the most similar sites to a site when needed (with a cache of the last site calculated)
 */
public class CalculatedSimilar implements Similar
{
    /**
     * Constructor
     * @param corr Method used to calculate correlation
     * @param data The genotype data
     * @param n The number of top hits to calculate
     */
    public CalculatedSimilar(Correlation corr, byte[][] data, int n)
    {
        this.corr = corr;
        this.data = data;
        this.n = n;
    }

    @Override
    public Integer[] getSimilar(int p)
    {
        if (p == cachedP.get())
        {
            return cached.get();
        }
        else
        {
            Integer[] a = new Integer[n];
            a = corr.topn(data,n,p).toArray(a);
            cachedP.set(p);
            cached.set(a);
            return a;
        }
    }

    private Correlation corr;
    private byte[][] data;
    private int n;

    private ThreadLocal<Integer> cachedP = ThreadLocal.withInitial(() -> -1);
    private ThreadLocal<Integer[]> cached = ThreadLocal.withInitial(() -> null);
}
