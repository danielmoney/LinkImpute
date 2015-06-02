package Methods;

import Mask.Mask;
import Utils.Value;
import java.util.List;
import java.util.Map;

public class KnniLDOpt implements Value
{
    
    public KnniLDOpt(byte[][] ob, Mask mask, Map<Integer,List<Integer>> sim)
    {
        this(ob,mask,sim,false);
    }
    
    public KnniLDOpt(byte[][] ob, Mask mask, Map<Integer,List<Integer>> sim,
            boolean verbose)
    {
        this.ob = ob;
        this.mask = mask;
        this.sim = sim;
        this.verbose = verbose;
        if (verbose)
        {
            System.out.println("\tk\tl\tAccuracy");
        }
        else
        {
            System.out.print("\t");
        }
    }
    
    public double value(int[] p)
    {
        KnniLD knnild = new KnniLD(sim,p[0],p[1]);
        //double v = knnild.fastAccuracy(orig, mask);
        long start = System.currentTimeMillis();
        double v = knnild.fastAccuracy(ob, mask);
        long time = (System.currentTimeMillis() - start) / 1000;
        if (verbose)
        {
            System.out.println("\t" + p[0] + "\t" + p[1] + "\t" + v + "\t\t(" + time + " seconds)");
        }
        else
        {
            System.out.print(".");
        }
        return v;
    }
    
    private byte[][] ob;
    private Mask mask;
    private Map<Integer,List<Integer>> sim;
    private boolean verbose;
}
