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

package Utils;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Comparator to sort integer indicies based on the corresponding value in a
 * double array.
 * @author Daniel Money
 */
public class SortByIndexDouble implements Comparator<Integer>
{
    /**
     * Constructor.  Defaults to sorting in standard order.
     * @param values The double array used to sort the indicies
     */
    public SortByIndexDouble(double[] values)
    {
        this.values = values;
        this.reverse = false;
    }
    
    /**
     * Constructor.  Can sort in either standard or reverse order.
     * @param values The double array used to sort the indicies
     * @param reverse If true sorts in reverse order
     */
    public SortByIndexDouble(double[] values, boolean reverse)
    {
        this.values = values;
        this.reverse = reverse;
    }

    @Override
    public int compare(Integer i, Integer j)
    {
        if (reverse)
        {
            return Double.compare(values[j], values[i]);
        }
        else
        {
            return Double.compare(values[i], values[j]);
        }
    }

    /**
     * Produce a sorted list of indicies for the entire array
     * @return Sorted list of indicies
     */
    public Integer[] sort()
    {
        Integer[] index = new Integer[values.length];
        for (int i = 0; i < index.length; i++)
        {
            index[i] = i;
        }
        Arrays.sort(index,this);
        return index;
    }

    private final double[] values;
    private final boolean reverse;
}    

