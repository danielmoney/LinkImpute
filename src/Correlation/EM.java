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
 * Calculates LD using the expectation maximization method
 * @author Daniel Money
 */
public class EM extends Correlation
{
    @Override
    public double calculate(byte[] d1, byte[] d2)
    {
        int[][] counts = new int[3][3];
        int counta = 0; int countb = 0;
        for (int i = 0; i < d1.length; i++)
        {
            if ((d1[i] >= 0) && (d2[i] >= 0))
            {
                counts[d1[i]][d2[i]] ++;
            }
            //Not sure whether these should be in these if statements or the
            //one above
            if (d1[i] >= 0)
            {
                counta += d1[i];
            }
            if (d2[i] >= 0)
            {
                countb += d2[i];
            }
        }
                    
        double pA = 1.0 - 0.5 * (double) counta / (double) d1.length;
        double pB = 1.0 - 0.5 * (double) countb / (double) d2.length;
        
        double pAB = maxpAB(counts, pA, pB);
        
        return calculateLD(pA, pB, pAB);
    }
    
    private double maxpAB(int[][] counts, double pA, double pB)
    {
        double a = pA * pB + Math.max(-pA * pB, -(1.0-pA) * (1.0-pB));
        double b = pA * pB + Math.min(pA * (1.0-pB), pB * (1.0-pA));

        double c = a + R*(b-a); double cl = l(c,counts,pA,pB);
        do
        {
            double d = c + R*(b-c); double dl = l(d,counts,pA,pB);
            
            if (cl >= dl)
            {
                b = a;
                a = d;
            }
            else
            {
                a = c;
                c = d;
                cl = dl;
            }
        }
        while (Math.abs(b - a) > 1e-4);
        
        return (b+a) / 2.0;
    }
    
    private double calculateLD(double pA, double pB, double pAB)
    {
        double D = pAB - pA * pB;
        
        double ld = (D * D) / (pA * (1.0 - pA) * pB * (1.0 - pB));
        return ld;
    }
    
    private double l(double pAB, int[][] counts, double pA, double pB)
    {
        double pAb = pA - pAB;
        double paB = pB - pAB;
        double pab = 1 - pA - pB + pAB;
        
        return  (2 * counts[0][0] + counts[0][1] + counts[1][0]) * Math.log(pAB) +
                (2 * counts[0][2] + counts[0][1] + counts[1][2]) * Math.log(pAb) +
                (2 * counts[2][0] + counts[1][0] + counts[2][1]) * Math.log(paB) +
                (2 * counts[2][2] + counts[1][2] + counts[2][1]) * Math.log(pab) +
                counts[1][1] * Math.log(pAB * pab + pAb * paB);
        
        /* This is the following rearranged and simplified:
         *      counts[0][0] * Math.log(pAB * pAB) +
         *      counts[0][1] * Math.log(pAB * pAb) +
         *      counts[0][2] * Math.log(pAb * pAb) +
         *      counts[1][0] * Math.log(PAB * paB) +
         *      counts[1][1] * Math.log(PAB * pab + PAb * paB) +
         *      counts[1][2] * Math.log(PAb * Pab) +
         *      counts[2][0] * Math.log(paB * paB) +
         *      counts[2][1] * Math.log(paB * pab) +
         *      counts[2][2] * Math.log(pab * pab)
         */                
    }
    
    //NOT SURE WHAT TO DO ABOUT THIS!    
    /**
     * Calculates LD between two SNPs
     * @param d1 SNP 1 genotype
     * @param d2 SNP 2 genotype
     * @return LD between the two SNPs
     */
    /*public static double calculate(BufferByteArray d1, BufferByteArray d2)
    {
        int[][] counts = new int[3][3];
        int counta = 0; int countb = 0;
        for (int i = 0; i < d1.size(); i++)
        {
            if ((d1.get(i) >= 0) && (d2.get(i) >= 0))
            {
                counts[d1.get(i)][d2.get(i)] ++;
            }
            //Not sure whether these should be in these if statements or the
            //one above
            if (d1.get(i) >= 0)
            {
                counta += d1.get(i);
            }
            if (d2.get(i) >= 0)
            {
                countb += d2.get(i);
            }
        }
                    
        double pA = 1.0 - 0.5 * (double) counta / (double) d1.size();
        double pB = 1.0 - 0.5 * (double) countb / (double) d2.size();
        
        double pAB = maxpAB(counts, pA, pB);
        
        return calculateLD(pA, pB, pAB);
    }*/
    
    private static final double R = (3.0 - Math.sqrt(5.0)) / 2.0;    
}
