package Mask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Mask
{
    public Mask(File f) throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(f));
        ArrayList<boolean[]> masklist = new ArrayList<>();
        list = null;
        String line;
        while ((line = in.readLine()) != null)
        {
            char[] chars = line.toCharArray();
            boolean[] b = new boolean[chars.length];
            for (int i = 0; i < chars.length; i++)
            {
                if (chars[i] == '0')
                {
                    b[i] = false;
                }
                else
                {
                    b[i] = true;
                }
            }
            masklist.add(b);
        }
        in.close();
        mask = new boolean[masklist.size()][];
        mask = masklist.toArray(mask);
    }
    
    public Mask(byte[][] orig, int number)
    {
        Random r = new Random();
        int n = 0;
        int al = orig.length;
        int bl = orig[0].length;
        mask = new boolean[al][bl];
        while (n < number)
        {
            int a = r.nextInt(al);
            int b = r.nextInt(bl);
            
            if ((orig[a][b] >= 0) && (!mask[a][b]))
            {
                mask[a][b] = true;
                n++;
            }
        }
        list = null;
    }
    
    public List<SampleSnp> getList()
    {
        if (list == null)
        {
            list = new ArrayList<>();
            for (int sample = 0; sample < mask.length; sample++)
            {
                boolean[] m = mask[sample];
                for (int snp = 0; snp < m.length; snp ++)
                {
                    if (m[snp])
                    {
                        list.add(new SampleSnp(sample,snp));
                    }
                }
            }
        }
        return list;
    }
    
    public double accuracy(byte[][] orig, byte[][] imputed)
    {
        int c = 0;
        int t = 0;
        for (int i = 0; i < orig.length; i++)
        {
            for (int j = 0; j < orig[0].length; j++)
            {
                if (mask[i][j])
                {
                    t++;
                    if (orig[i][j] == imputed[i][j])
                    {
                        c++;
                    }
                }
            }
        }
        
        return (double) c / (double) t;
    }
    
    public boolean[][] getArray()
    {
        return mask;
    }
    
    public byte[][] mask(byte[][] orig)
    {
        int al = orig.length;
        int bl = orig[0].length;
        byte[][] masked = new byte[al][bl];
        for (int a = 0; a < al; a++)
        {
            for (int b = 0; b < bl; b++)
            {
                if (mask[a][b])
                {
                    masked[a][b] = -1;
                }
                else
                {
                    masked[a][b] = orig[a][b];
                }
                
            }
        }
        return masked;
    }
    
    public void saveToFile(File f) throws IOException
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        for (boolean[] mm: mask)
        {
            for (boolean m: mm)
            {
                if (m)
                {
                    out.print("1");
                }
                else
                {
                    out.print("0");
                }
            }
            out.println();
        }
        out.close();
    }
    
    private boolean[][] mask;
    private List<SampleSnp> list;
}