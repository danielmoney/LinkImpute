package Executable;

import Exceptions.DataException;
import Exceptions.NotEnoughGenotypesException;
import Methods.KnniLD;
import java.util.List;
import java.util.Map;

public class BestKL
{
    //protected static KL bestKL(byte[][] orig, int number, Map<Integer,Set<Integer>> ld) throws DataException, NotEnoughGenotypesException
    protected static KL bestKL(byte[][] orig, int number, Map<Integer,List<Integer>> ld) throws DataException, NotEnoughGenotypesException
    {
        System.out.println("Calculating best k and l...");
        //boolean[][] mask = generateMask(orig,number);
        //byte[][] masked = generateMasked(orig,mask);
        Mask mask = new Mask(orig,number);
        byte[][] masked = mask.mask(orig);
        
        int a = 1;
        LAcc aAcc = bestL(orig,masked,mask,ld,a);
        int b = 5;
        LAcc bAcc = bestL(orig,masked,mask,ld,b);
        int c = 9;
        LAcc cAcc = bestL(orig,masked,mask,ld,c);
        
        while (bAcc.getAcc() < cAcc.getAcc())
        {
            a = a + 4;
            aAcc = bAcc;
            b = b + 4;
            bAcc = cAcc;
            c = c + 4;
            cAcc = bestL(orig,masked,mask,ld,c);
        }
        
        int d = (a + b) / 2;
        LAcc dAcc = bestL(orig,masked,mask,ld,d);
        int e = (b + c) / 2;
        LAcc eAcc = bestL(orig,masked,mask,ld,e);
        
        if ((dAcc.getAcc() > bAcc.getAcc()))
        {
            c = b;
            cAcc = bAcc;
            b = d;
            bAcc = dAcc;
        }
        if ((eAcc.getAcc() > bAcc.getAcc()))
        {
            a = b;
            aAcc = bAcc;
            b = e;
            bAcc = eAcc;
        }
        if ((dAcc.getAcc() < bAcc.getAcc()) && (eAcc.getAcc() < bAcc.getAcc()))
        {
            a = d;
            aAcc = dAcc;
            c = e;
            cAcc = eAcc;
        }
        
        d = (a + b) / 2;
        dAcc = bestL(orig,masked,mask,ld,d);
        e = (b + c) / 2;
        eAcc = bestL(orig,masked,mask,ld,e);
        
        LAcc maxAcc = aAcc;
        int maxK = a;
        if (bAcc.getAcc() > maxAcc.getAcc())
        {
            maxAcc = bAcc;
            maxK = b;
        }
        if (cAcc.getAcc() > maxAcc.getAcc())
        {
            maxAcc = cAcc;
            maxK = c;
        }
        if (dAcc.getAcc() > maxAcc.getAcc())
        {
            maxAcc = dAcc;
            maxK = d;
        }
        if (eAcc.getAcc() > maxAcc.getAcc())
        {
            maxAcc = eAcc;
            maxK = e;
        }
        
        System.out.println("Best k: " + maxK + "\tBest l: " + maxAcc.getL() + "\tAccuracy: " + maxAcc.getAcc());
        return new KL(maxK,maxAcc.getL());
    }
    
    protected static LAcc bestL(byte[][] orig, byte[][] masked, Mask mask,
    //        Map<Integer,Set<Integer>> ld, int k) throws DataException, NotEnoughGenotypesException
    Map<Integer,List<Integer>> ld, int k) throws DataException, NotEnoughGenotypesException
    {
        int a = 1;
        double aAcc = singleKL(orig,masked,mask,ld,k,a);
        int b = 5;
        double bAcc = singleKL(orig,masked,mask,ld,k,b);
        int c = 9;
        double cAcc = singleKL(orig,masked,mask,ld,k,c);
        
        while (bAcc < cAcc)
        {
            a = a + 4;
            aAcc = bAcc;
            b = b + 4;
            bAcc = cAcc;
            c = c + 4;
            cAcc = singleKL(orig,masked,mask,ld,k,c);
        }
        
        int d = (a + b) / 2;
        double dAcc = singleKL(orig,masked,mask,ld,k,d);
        int e = (b + c) / 2;
        double eAcc = singleKL(orig,masked,mask,ld,k,e);
        
        if ((dAcc > bAcc))
        {
            c = b;
            cAcc = bAcc;
            b = d;
            bAcc = dAcc;
        }
        if ((eAcc > bAcc))
        {
            a = b;
            aAcc = bAcc;
            b = e;
            bAcc = eAcc;
        }
        if ((dAcc < bAcc) && (eAcc < bAcc))
        {
            a = d;
            aAcc = dAcc;
            c = e;
            cAcc = eAcc;
        }
        
        d = (a + b) / 2;
        dAcc = singleKL(orig,masked,mask,ld,k,d);
        e = (b + c) / 2;
        eAcc = singleKL(orig,masked,mask,ld,k,e);
        
        double maxAcc = aAcc;
        int maxL = a;
        if (bAcc > maxAcc)
        {
            maxAcc = bAcc;
            maxL = b;
        }
        if (cAcc > maxAcc)
        {
            maxAcc = cAcc;
            maxL = c;
        }
        if (dAcc > maxAcc)
        {
            maxAcc = dAcc;
            maxL = d;
        }
        if (eAcc > maxAcc)
        {
            maxAcc = eAcc;
            maxL = e;
        }
        
        return new LAcc(maxL,maxAcc);
    }
    
    //private static double singleKL(byte[][] orig, byte[][] masked, Mask mask, Map<Integer,Set<Integer>> ld, int k, int l)
    private static double singleKL(byte[][] orig, byte[][] masked, Mask mask, Map<Integer,List<Integer>> ld, int k, int l)
            throws DataException, NotEnoughGenotypesException
    {
        System.out.print("\tk=" + k + "\tl=" + l + "\t");
        KnniLD knnild = new KnniLD(ld,k,l);
        byte[][] imputed = knnild.compute(masked);
        double acc = mask.accuracy(orig,imputed);
        System.out.println("Accuracy: " + acc);
        return acc;
    }
    
    protected static class KL
    {
        private KL(int k, int l)
        {
            this.k = k;
            this.l = l;
        }
        
        protected int getK()
        {
            return k;
        }
        
        protected int getL()
        {
            return l;
        }
        
        private int k;
        private int l;
    }
    
    private static class LAcc
    {
        private LAcc(int l, double acc)
        {
            this.l = l;
            this.acc = acc;
        }
        
        protected int getL()
        {
            return l;
        }
        
        protected double getAcc()
        {
            return acc;
        }
        
        private int l;
        private double acc;
    }
}
