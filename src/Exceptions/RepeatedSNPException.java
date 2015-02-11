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
 * Exception to be thrown if a SNP name is repeated
 * @author Daniel Money
 */
public class RepeatedSNPException extends DataException
{

    /**
     * Constructor
     * @param s Name of the SNP that is repeated
     */
    public RepeatedSNPException(String s)
    {
        super("The SNP " + s + " occurs twice");
    }
}
