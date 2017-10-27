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
 * Exception to be thrown if a sample has the wrong number of SNPs
 * @author Daniel Money
 */
public class WrongNumberOfSNPsException extends DataException
{

    /**
     * Constructor for use when the sample name is known
     * @param s The name of the sample with the wrong number of SNPs
     */
    public WrongNumberOfSNPsException(String s)
    {
        super("Wrong number of SNPS on line beginning: " + s);
    }
    
    /**
     * Constructor for use when the sample name is not known
     * @param i The line of the file corresponding to the sample with the wrong
     * number of SNPs
     */
    public WrongNumberOfSNPsException(int i)
    {
        super("Wrong number of SNPS on line " + i);
    }
}
