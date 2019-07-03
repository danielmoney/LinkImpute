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
 * Over arching exception for data related problems
 * @author Daniel Money
 */
public class DataException extends Exception
{
    /**
     * Constructs an instance of {@code DataException} with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public DataException(String msg)
    {
        super(msg);
    }
}
