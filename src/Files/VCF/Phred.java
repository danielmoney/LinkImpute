package Files.VCF;

public class Phred
{
    public static double toPhred(double p)
    {
        return -10 * Math.log10(p);
    }
    
    public static double fromPhred(double p)
    {
        return Math.pow(10,-p/10.0);
    }
}
