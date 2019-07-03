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

import java.util.List;
import java.util.Map;

/**
 * Stores the sites most similar to a site for all sites
 */
public class StoredSimilar implements Similar
{
    /**
     * Constructor
     * @param topn Pre-calculated top similar sites for each site (map from site to list of top similar sites)
     */
    public StoredSimilar(Map<Integer, List<Integer>> topn)
    {
        sim = new Integer[topn.size()][];
        for (Map.Entry<Integer,List<Integer>> e: topn.entrySet())
        {
            Integer[] a = new Integer[e.getValue().size()];
            sim[e.getKey()] = e.getValue().toArray(a);
        }
    }

    public Integer[] getSimilar(int p)
    {
        return sim[p];
    }

    private Integer[][] sim;
}
