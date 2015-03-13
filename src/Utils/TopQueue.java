package Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

public class TopQueue<E, V extends Comparable>
{
    public TopQueue(int top)
    {
        this(top,false);
    }
    
    public TopQueue(int top, boolean reverse)
    {
        this.top = top;
        this.map = new HashMap<>(top+1);
        this.set = new TreeSet<>(new Compare<>(map,reverse));
    }
    
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
    
    /*public NavigableSet<E> getSet()
    {
        return set;
    }
    
    public NavigableSet<E> getSet(int n)
    {
        E lim = null;
        int c = 0;
        for (E e: set)
        {
            c++;
            if (c == n)
            {
                lim = e;
            }
        }
        return set.headSet(lim, true);
    }*/
    
    public List<E> getList()
    {
        List<E> list = new ArrayList<>(top);
        for (E e: set)
        {
            list.add(e);
        }
        return list;
    }
    
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
    
    public Map<E,V> getMap()
    {
        return map;
    }
    
    private class Compare<E,V extends Comparable<V>> implements Comparator<E>
    {
        public Compare(Map<E,V> map, boolean reverse)
        {
            this.map = map;
            this.reverse = reverse;
        }        
        
        public int compare(E e1, E e2)
        {
            int c = map.get(e2).compareTo(map.get(e1));
            if (c != 0)
            {
                if (reverse)
                {
                    return c;//map.get(e2).compareTo(map.get(e1));
                }
                else
                {
                    return -c;//map.get(e1).compareTo(map.get(e2));
                }
            }
            else
            {
                if ((e1 instanceof Comparable) && (e2 instanceof Comparable))
                {
                    Comparable c1 = (Comparable) e1;
                    Comparable c2 = (Comparable) e2;
                    return c1.compareTo(c2);
                }
                else
                {
                    return 0;
                }
            }
        }
        
        private final Map<E,V> map;
        private final boolean reverse;
    }
    
    private final int top;
    private final TreeSet<E> set;
    private final HashMap<E,V> map;
}
