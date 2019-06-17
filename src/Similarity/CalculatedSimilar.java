package Similarity;

import Correlation.Correlation;

public class CalculatedSimilar implements Similar
{
    public CalculatedSimilar(Correlation corr, byte[][] data, int n)
    {
        this.corr = corr;
    }

    @Override
    public Integer[] getSimilar(int p)
    {
        if (p == cachedP)
        {
            return cached;
        }
        else
        {
            Integer[] a = new Integer[n];
            a = corr.topn(data,n,p).toArray(a);
            return a;
        }
    }

    private Correlation corr;
    private byte[][] data;
    private int n;

    private int cachedP;
    private Integer[] cached;
}
