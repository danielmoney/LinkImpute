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

package Exceptions;

/**
 * Exception for when there aren't enough genotypes at a SNP to impute.
 * @author Daniel Money
 */
public class NotEnoughGenotypesException extends Exception
{

    /**
     * Constructor.
     * @param snp The SNP that has the problem.
     * @param k The number of genotypes needed
     */
    public NotEnoughGenotypesException(int snp, int k)
    {
        super("Fewer than " + k + " genotypes avaliable for imputation at SNP " + snp);
    }
}
