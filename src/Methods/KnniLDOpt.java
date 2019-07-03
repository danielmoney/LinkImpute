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

import Mask.Mask;
import Similarity.Similar;
import Similarity.StoredSimilar;
import Utils.Value;
import java.util.List;
import java.util.Map;

/**
 * Wrapper around KnniLD to allow optimization of parameters
 * @author Daniel Money
 */
public class KnniLDOpt implements Value
{
    
    /**
     * Constructor
     * @param orig The original matrix
     * @param mask The mask
     * @param sim Object for accessing similarity between SNPs
     */
    public KnniLDOpt(byte[][] orig, Mask mask,Similar sim)
    {
        this(orig,mask,sim,false);
    }
    
    /**
     * Constructor
     * @param orig The original matrix
     * @param mask The mask
     * @param sim  Object for accessing similarity between SNPs
     * @param verbose Verbose output to standard out?
     */
    public KnniLDOpt(byte[][] orig, Mask mask, Similar sim,
            boolean verbose)
    {
        this.orig = orig;
        this.mask = mask;
        this.sim = sim;
        this.verbose = verbose;
        if (verbose)
        {
            System.out.println("\tk\tl\tAccuracy");
        }
        else
        {
            System.out.print("\t");
        }
    }
    
    @Override
    public double value(int[] p)
    {
        KnniLD knnild = new KnniLD(sim,p[0],p[1]);
        long start = System.currentTimeMillis();
        double v = knnild.fastAccuracy(orig, mask);
        long time = (System.currentTimeMillis() - start) / 1000;
        if (verbose)
        {
            System.out.println("\t" + p[0] + "\t" + p[1] + "\t" + v + "\t\t(" + time + " seconds)");
        }
        else
        {
            System.out.print(".");
        }
        return v;
    }
    
    private byte[][] orig;
    private Mask mask;
    private Similar sim;
    private boolean verbose;
}
