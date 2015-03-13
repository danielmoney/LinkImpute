package Correlation;

public class Pearson extends Correlation
{
    public double calculate(byte[] d1, byte[] d2)
    {
        int[][] counts = new int[3][3];
        int c = 0;
        for (int i = 0; i < d1.length; i++)
        {
            if ((d1[i] >= 0) && (d2[i] >= 0))
            {
                counts[d1[i]][d2[i]] ++;
            }
            c++;
        }
        
        int tota = counts[1][0] + counts[1][1] + counts[1][2] +
                2 * (counts[2][0] + counts[2][1] + counts[2][2]);
        double meana = (double) tota / (double) c;
        
        int totb = counts[0][1] + counts[1][1] + counts[2][1] +
                2 * (counts[0][2] + counts[1][2] + counts[2][2]);
        double meanb = (double) totb / (double) c;
        
        double xy = 0.0;
        double xx = 0.0;
        double yy = 0.0;
        
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                xy += (double) counts[i][j] * ((double) i - meana) * ((double) j - meanb);
                xx += (double) counts[i][j] * ((double) i - meana) * ((double) i - meana);
                yy += (double) counts[i][j] * ((double) j - meanb) * ((double) j - meanb);
            }
        }
        
        return (xy * xy) / (xx * yy);
    }
}
