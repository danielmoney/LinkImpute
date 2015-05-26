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

/**
 * Calculates LD as a simple Pearson correlation
 * @author Daniel Money
 */
public class Pearson extends Correlation
{
    @Override
    public double calculate(byte[] d1, byte[] d2)
    {
        int[][] counts = new int[3][3];
        int c = 0;
        for (int i = 0; i < d1.length; i++)
        {
            if ((d1[i] >= 0) && (d2[i] >= 0))
            {
                counts[d1[i]][d2[i]] ++;
            }
            c++;
        }
        
        int tota = counts[1][0] + counts[1][1] + counts[1][2] +
                2 * (counts[2][0] + counts[2][1] + counts[2][2]);
        double meana = (double) tota / (double) c;
        
        int totb = counts[0][1] + counts[1][1] + counts[2][1] +
                2 * (counts[0][2] + counts[1][2] + counts[2][2]);
        double meanb = (double) totb / (double) c;
        
        double xy = 0.0;
        double xx = 0.0;
        double yy = 0.0;
        
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                xy += (double) counts[i][j] * ((double) i - meana) * ((double) j - meanb);
                xx += (double) counts[i][j] * ((double) i - meana) * ((double) i - meana);
                yy += (double) counts[i][j] * ((double) j - meanb) * ((double) j - meanb);
            }
        }
        
        // If we have an invariant site then xx or yy will be zero, in which
        // case return a correlation of zero.  This ensures invariant sites are
        // sorted to the bottom of the list of LD sites and so aren't used in
        // distance calculations (which makes sense since they add no information
        // to the distance.
        if ((xx == 0.0) || (yy == 0.0))
        {
            return 0;
        }
        
        return (xy * xy) / (xx * yy);
    }
}
