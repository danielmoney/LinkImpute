package Mask;

public class SampleSnp
{
    public SampleSnp(int sample, int snp)
    {
        this.sample = sample;
        this.snp = snp;
    }

    public int getSample()
    {
        return sample;
    }

    public int getSnp()
    {
        return snp;
    }

    private final int sample;
    private final int snp;
}
