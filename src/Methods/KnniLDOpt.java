package Methods;

import Mask.Mask;
import Utils.BufferByteArray2D;
import Utils.Value;
import java.util.List;
import java.util.Map;

public class KnniLDOpt implements Value
{
    public KnniLDOpt(byte[][] orig, Mask mask, Map<Integer,List<Integer>> sim)
    {
        this(new BufferByteArray2D(orig),mask,sim,false);
    }
    
    public KnniLDOpt(byte[][] orig, Mask mask, Map<Integer,List<Integer>> sim,
            boolean verbose)
    {
        this(new BufferByteArray2D(orig),mask,sim,false);
    }
    
    public KnniLDOpt(BufferByteArray2D ob, Mask mask, Map<Integer,List<Integer>> sim)
    {
        this(ob,mask,sim,false);
    }
    
    public KnniLDOpt(BufferByteArray2D ob, Mask mask, Map<Integer,List<Integer>> sim,
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
        double v = knnild.fastAccuracy(ob, mask);
        if (verbose)
        {
            System.out.println("\t" + p[0] + "\t" + p[1] + "\t" + v);
        }
        else
        {
            System.out.print(".");
        }
        return v;
    }
    
    private BufferByteArray2D ob;
    private Mask mask;
    private Map<Integer,List<Integer>> sim;
    private boolean verbose;
}
