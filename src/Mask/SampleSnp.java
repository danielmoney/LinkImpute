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

package Mask;

/**
 * Represents the position of a single genotype (i.e. as indexed by a sample
 * and a snp)
 * @author Daniel Money
 */
public class SampleSnp
{

    /**
     * Default constructor
     * @param sample  The position of the sample
     * @param snp The position of the snp
     */
    public SampleSnp(int sample, int snp)
    {
        this.sample = sample;
        this.snp = snp;
    }

    /**
     * Get the sample
     * @return The position of the sample
     */
    public int getSample()
    {
        return sample;
    }

    /**
     * Get the SNP
     * @return The position of the SNP
     */
    public int getSnp()
    {
        return snp;
    }

    private final int sample;
    private final int snp;
}
