package Similarity;

import java.util.List;
import java.util.Map;

public class StoredSimilar implements Similar
{
    public StoredSimilar(Map<Integer, List<Integer>> topn)
    {
        sim = new Integer[topn.size()][];
        for (Map.Entry<Integer,List<Integer>> e: topn.entrySet())
        {
            Integer[] a = new Integer[e.getValue().size()];
            sim[e.getKey()] = e.getValue().toArray(a);
        }
    }

    public Integer[] getSimilar(int p)
    {
        return sim[p];
    }

    private Integer[][] sim;
}
