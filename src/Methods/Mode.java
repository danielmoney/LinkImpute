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

/**
 * Class to perform Mode imputation
 * @author Daniel Money
 */
public class Mode
{

    /**
     * Created a class to perform Mode imputation
     */
    public Mode()
    {
    }
    
    /**
     * Impute missing data
     * @param original The original data set.  Missing data is coded as -1
     * @return The imputed data set.
     */
    public byte[][] compute(byte[][] original)
    {
        // Create counts of each of the three genotypes at each SNP
        int[][] count = new int[original[0].length][3];
        for (int i = 0; i < original.length; i++)
        {
            byte[] o = original[i];
            for (int j = 0; j < o.length; j++)
            {
                if (o[j] >= 0)
                {
                    count[j][o[j]]++;
                }
            }
        }
        
        // Store the genotype with the maximum count for each SNP (i.e. the modal
        // value.
        byte[] max = new byte[count.length];
        for (int i = 0; i < count.length; i++)
        {
            byte mi = 0;
            int mv = 0;
            int[] c = count[i];
            for (byte j = 0; j < 3; j++)
            {
                if (c[j] > mv)
                {
                    mi = j;
                    mv = c[j];
                }
            }
            max[i] = mi;
        }
        
        // Loop through every genotype.  If it is missing (i.e. -1) replace it
        // with the modal value.
        byte[][] ret = new byte[original.length][];
        for (int i = 0; i < original.length; i++)
        {
            byte[] o = original[i];
            ret[i] = new byte[o.length];
            for (int j = 0; j < o.length; j++)
            {
                if (o[j] >= 0)
                {
                    ret[i][j] = o[j];
                }
                else
                {
                    ret[i][j] = max[j];
                }
            }
        }
        
        return ret;
    }
    
    public double fastAccuracy(byte[][] original, Mask mask) throws NotEnoughGenotypesException, WrongNumberOfSNPsException
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
                    byte imputed = impute(original, j);
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

    private byte impute(byte[][] original, int p)
    {
        int[] count = new int[3];
        for (int j = 0; j < original.length; j++)
        {
            if (original[j][p] >= 0)
            {
                count[original[j][p]]++;
            }
        }
        
        byte mi = 0;
        int mv = 0;
        for (byte j = 0; j < 3; j++)
        {
            if (count[j] > mv)
            {
                mi = j;
                mv = count[j];
            }
        }
        return mi;
    }
}
