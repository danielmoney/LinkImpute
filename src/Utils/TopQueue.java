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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Keeps track of the top n based on their assigned value.  IN case of ties
 * entries are sorted by the value of the entry.
 * @author Daniel Money
 * @param <E> The type of the entry.
 * @param <V> The type of the value.
 */
public class TopQueue<E extends Comparable<E>, V extends Comparable<V>>
{

    /**
     * Constructor
     * @param top The number of top entries to store
     */
    public TopQueue(int top)
    {
        this(top,false);
    }
    
    /**
     * Constructor
     * @param top The number of top entries to store
     * @param reverse Reverse the ordering of values
     */
    public TopQueue(int top, boolean reverse)
    {
        this.top = top;
        this.map = new HashMap<>(top+1);
        this.set = new TreeSet<>(new Compare<>(map,reverse));
    }
    
    /**
     * Attempts to add a new entry
     * @param e The entry
     * @param v The entry's value
     * @return Whether the entry was added.  Returns false if the entry
     * was smaller than the smallest element already in the queue and the
     * queue is full.
     */
    public synchronized boolean add(E e, V v)
    {
        map.put(e, v);
        set.add(e);
        if (set.size() > top)
        {
            E removed = set.pollLast();
            map.remove(removed);
            return !removed.equals(e);
        }
        else
        {
            return true;
        }
    }
   
    /**
     * Returns the entries in the queue as an ordered list
     * @return Ordered list of entries
     */
    public List<E> getList()
    {
        List<E> list = new ArrayList<>(top);
        for (E e: set)
        {
            list.add(e);
        }
        return list;
    }
    
    /**
     * Returns the entries in the queue as an ordered list.  Only the top n 
     * entries are returned, or all the entries in the list if n is greater
     * than the size of the queue
     * @param n The number of entries to return
     * @return Ordered list of entries
     */
    public List<E> getList(int n)
    {
        List<E> list = new ArrayList<>(n);
        int c = 0;
        for (E e: set)
        {
            if (c < n)
            {
                list.add(e);
            }
            c++;
        }
        return list;
    }
    
    /**
     * Get a (unsorted) map of entries and their values.
     * @return Map of entries and values.
     */
    public Map<E,V> getMap()
    {
        return map;
    }
    
    private class Compare<E extends Comparable<E>,V extends Comparable<V>> implements Comparator<E>
    {
        public Compare(Map<E,V> map, boolean reverse)
        {
            this.map = map;
            this.reverse = reverse;
        }        
        
        @Override
        public int compare(E e1, E e2)
        {
            int c = map.get(e2).compareTo(map.get(e1));
            if (c != 0)
            {
                if (reverse)
                {
                    return c;
                }
                else
                {
                    return -c;
                }
            }
            else
            {
                return e1.compareTo(e2);
            }
        }
        
        private final Map<E,V> map;
        private final boolean reverse;
    }
    
    private final int top;
    private final TreeSet<E> set;
    private final HashMap<E,V> map;
}
