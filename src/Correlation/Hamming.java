package Correlation;

public class Hamming extends Correlation
{
    public double calculate(byte[] d1, byte[] d2)
    {
        int c = 0;
        if (d1.length != d2.length)
        {
            return -1;
        }
        for (int i  = 0; i < d1.length; i++)
        {
            if (d1[i] == d2[i])
            {
                c++;
            }
        }
        return (double) c / (double) d1.length;
    }
}
