package Executable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

public class Mask
{
    public Mask(File f) throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(f));
        ArrayList<boolean[]> list = new ArrayList<>();
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
            list.add(b);
        }
        in.close();
        mask = new boolean[list.size()][];
        mask = list.toArray(mask);
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
        PrintStream out = new PrintStream(new FileOutputStream(f));
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
    
    /*protected static byte[][] generateMasked(byte[][] orig, boolean[][] mask)
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
    
    protected static boolean[][] generateMask(byte[][] orig, int number)
    {
        Random r = new Random();
        int n = 0;
        int al = orig.length;
        int bl = orig[0].length;
        boolean[][] mask = new boolean[al][bl];
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
        return mask;
    }
    
    protected static double accuracy(byte[][] orig, byte[][] imputed, boolean[][] masked)
    {
        int c = 0;
        int t = 0;
        for (int i = 0; i < orig.length; i++)
        {
            for (int j = 0; j < orig[0].length; j++)
            {
                if (masked[i][j])
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
    }*/
    
    protected static int getMaskNumber(byte[][] orig, String opt)
    {
        if (opt != null)
        {
            return Integer.parseInt(opt);
        }
        else
        {
            int tot = 0;
            for (int i = 0; i < orig.length; i++)
            {
                byte[] o = orig[i];
                for (int j = 0; j < o.length; j++)
                {
                    if (o[j] >= 0)
                    {
                        tot++;
                    }
                }
            }
            
            return (tot / 100);
        }
    }
}
