package Executable;

import Exceptions.DataException;
import Exceptions.NotEnoughGenotypesException;
import Methods.Knni;

public class BestK
{
    protected static int bestK(byte[][] orig, int number) throws DataException, NotEnoughGenotypesException
    {
        System.out.println("Calculating best k...");
        //boolean[][] mask = generateMask(orig,number);
        //byte[][] masked = generateMasked(orig,mask);
        Mask mask = new Mask(orig,number);
        byte[][] masked = mask.mask(orig);
        
        int a = 1;
        double aAcc = singleK(orig,masked,mask,a);
        int b = 5;
        double bAcc = singleK(orig,masked,mask,b);
        int c = 9;
        double cAcc = singleK(orig,masked,mask,c);
        
        while (bAcc < cAcc)
        {
            a = a + 4;
            aAcc = bAcc;
            b = b + 4;
            bAcc = cAcc;
            c = c + 4;
            cAcc = singleK(orig,masked,mask,c);
        }
        
        int d = (a + b) / 2;
        double dAcc = singleK(orig,masked,mask,d);
        int e = (b + c) / 2;
        double eAcc = singleK(orig,masked,mask,e);
        
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
        dAcc = singleK(orig,masked,mask,d);
        e = (b + c) / 2;
        eAcc = singleK(orig,masked,mask,e);
        
        double maxAcc = aAcc;
        int maxK = a;
        if (bAcc > maxAcc)
        {
            maxAcc = bAcc;
            maxK = b;
        }
        if (cAcc > maxAcc)
        {
            maxAcc = cAcc;
            maxK = c;
        }
        if (dAcc > maxAcc)
        {
            maxAcc = dAcc;
            maxK = d;
        }
        if (eAcc > maxAcc)
        {
            maxAcc = eAcc;
            maxK = e;
        }
        
        System.out.println("Best k: " + maxK + "\tAccuracy: " + maxAcc);
        return maxK;
    }
    
    private static double singleK(byte[][] orig, byte[][] masked, Mask mask, int k) throws DataException, NotEnoughGenotypesException
    {
        System.out.print("\tk=" + k + "\t");
        Knni knni = new Knni(k);
        byte[][] imputed = knni.compute(masked);
        double acc = mask.accuracy(orig,imputed);
        System.out.println("Accuracy: " + acc);
        return acc;
    }    
}
