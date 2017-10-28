/*
 * This file is part of LinkImpute.
 * 
 * LinkImpute is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LinkImpute is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LinkImpute.  If not, see <http://www.gnu.org/licenses/>.
 */
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

/**
 * Represents a mask for calculating imputation accuracy.  Contains data on
 * which genotypes should be masked.
 * @author Daniel Money
 */
public class Mask
{

    /**
     * Creates a mask from file.
     * @param f The file to read the mask from
     * @throws IOException If there are problems reading the file
     */
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
                // 0 is unmasked (so false), anything else is masked (so true)
                b[i] = !(chars[i] == '0');
            }
            masklist.add(b);
        }
        in.close();
        mask = new boolean[masklist.size()][];
        mask = masklist.toArray(mask);
    }
    
    /**
     * Creates a mask for a dataset
     * @param orig The original dataset
     * @param number The number of genotypes to mask
     */
    public Mask(byte[][] orig, int number)
    {
        System.out.println("Masking " + number + " genotypes");
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
    
    /**
     * Returns a list of masked genotypes.  List is only created when this
     * function is first called to save memory.
     * @return A list of masked genotypes
     */
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
    
    /**
     * Calculates imputation accuracy
     * @param orig The original, unmasked dataset
     * @param imputed The imputed dataset (imputed on masked data)
     * @return Imputation accuracy
     */
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
    
    /**
     * Returns an array of masked genotypes (true = masked, false = unmasked).
     * @return An array of masked genotypes.
     */
    public boolean[][] getArray()
    {
        return mask;
    }
    
    /**
     * Creates a masked array using this mask
     * @param orig Array to be masked
     * @return Masked array
     */
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
    
    /**
     * Saves this mask to a file
     * @param f File to save to
     * @throws IOException If there are problems writing the file
     */
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