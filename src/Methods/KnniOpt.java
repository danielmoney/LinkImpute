package Methods;

import Exceptions.NotEnoughGenotypesException;
import Exceptions.WrongNumberOfSNPsException;
import Mask.Mask;
import Utils.Value;

public class KnniOpt implements Value
{
    public KnniOpt(byte[][] orig, Mask mask, double[][] weight)
    {
        this(orig,mask,weight,false);
    }
    
    public KnniOpt(byte[][] orig, Mask mask, double[][] weight, boolean verbose)
    {
        this.orig = orig;
        this.mask = mask;
        this.verbose = verbose;
        if (verbose)
        {
            System.out.println("\tk\tAccuracy");
        }
        
        this.weight = weight;
    }
    
    public double value(int[] k) throws NotEnoughGenotypesException,
           WrongNumberOfSNPsException
    {
        Knni knni = new Knni(k[0]);
        double v = knni.fastAccuracy(orig, mask, weight);
        if (verbose)
        {
            System.out.println("\t" + k[0] + "\t" + v);
        }
        return v;
    }
    
    private byte[][] orig;
    private Mask mask;
    private double[][] weight;
    private boolean verbose;
}
