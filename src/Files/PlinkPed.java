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

package Files;

import Exceptions.InvalidGenotypeException;
import Exceptions.RepeatedSNPException;
import Exceptions.RepeatedSampleException;
import Exceptions.WrongNumberOfSNPsException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class for dealing with data in the plink .ped format.
 * @author Daniel Money
 */
public class PlinkPed
{    
    /**
     * Constructor.  Loads data from a file.  Defaults to assuming there are six
     * columns of meta data before the genotype data
     * @param f File to load data from
     * @throws IOException If there is a problem reading from the file
     * @throws InvalidGenotypeException If there is an invalid genotype in the file
     * @throws RepeatedSNPException If a SNP name is repeated
     * @throws RepeatedSampleException If a Sample name is repeated
     * @throws WrongNumberOfSNPsException If a sample has the wrong number of SNPs
     */
    public PlinkPed(File f) throws IOException, InvalidGenotypeException,
            RepeatedSNPException, RepeatedSampleException, WrongNumberOfSNPsException
    {
        this(f,6);
    }
    
    /**
     * Constructor.  Loads data from a file.  Allows for a variable number of
     * meta columns.
     * @param f File to load data from
     * @param metacolumns The number of meta columns before genotype data begins
     * @throws IOException If there is a problem reading from the file
     * @throws InvalidGenotypeException If there is an invalid genotype in the file
     * @throws RepeatedSNPException If a SNP name is repeated
     * @throws RepeatedSampleException If a Sample name is repeated
     * @throws WrongNumberOfSNPsException If a sample has the wrong number of SNPs
     */
    public PlinkPed(File f, int metacolumns) throws IOException, InvalidGenotypeException,
            RepeatedSNPException, RepeatedSampleException, WrongNumberOfSNPsException
    {
        BufferedReader in = new BufferedReader(new FileReader(f));
                
        samples = new ArrayList<>();
        data = new HashMap<>();
        meta = new HashMap<>();
        
        int length = -1;
        String line;
        
        Map<Integer,Map<Character,Integer>> counts = new HashMap<>();
        Map<String,char[]> tempGeno = new HashMap<>();
        
        while ((line = in.readLine()) != null)
        {
            String[] parts = line.split("\\s");

            if (length == -1)
            {
                length = parts.length;
            }
            
            if (parts.length != length)
            {
                throw new WrongNumberOfSNPsException(parts[0]);
            }

            String s = parts[1];
            if (samples.contains(s))
            {
                throw new RepeatedSampleException(s);
            }
            else
            {
                samples.add(s);
            }

            meta.put(s, Arrays.copyOf(parts, metacolumns));
            
            char[] tg = new char[(parts.length - metacolumns) / 2];
            for (int i = 0; i < (parts.length - metacolumns) / 2; i++)
            {
                char c1 = parts[i*2 + metacolumns].charAt(0);
                char c2 = parts[i*2 + metacolumns + 1].charAt(0);
                char g = getTempGeno(parts[i*2 + metacolumns],parts[i*2 + metacolumns + 1]);
                tg[i] = g;
                if (counts.get(i) == null)
                {
                    counts.put(i, new HashMap<Character,Integer>());
                }
                
                if (c1 != '0')
                {
                    if (counts.get(i).get(c1) == null)
                    {
                        counts.get(i).put(c1, 0);
                    }
                    counts.get(i).put(c1, counts.get(i).get(c1) + 1);
                }
                
                if (c2 != '0')
                {
                    if (counts.get(i).get(c2) == null)
                    {
                        counts.get(i).put(c2, 0);
                    }
                    counts.get(i).put(c2, counts.get(i).get(c2) + 1);
                }

            }
            tempGeno.put(s,tg);
        }
        
        major = new HashMap<>();
        minor = new HashMap<>();
        for (int i = 0; i < counts.size(); i++)
        {
            Map<Character,Integer> c = counts.get(i);
            int maj = 0;
            int min = 0;
            major.put(i,null);
            minor.put(i,null);
            for (Entry<Character,Integer> e: c.entrySet())
            {
                if (e.getValue() > maj)
                {
                    min = maj;
                    maj = e.getValue();
                    minor.put(i, major.get(i));
                    major.put(i, e.getKey());
                }
                else if (e.getValue() > min)
                {
                    min = e.getValue();
                    minor.put(i,e.getKey());
                }

            }
        }

        in.close();
        
        for (String s: samples)
        {
            Map<Integer,Byte> d = new HashMap<>();
            char[] tg = tempGeno.get(s);
            for (int i = 0; i < tg.length; i++)
            {
                d.put(i,getGeno(tg[i],major.get(i)));
            }
            data.put(s,d);
        }
    }
    
    private byte getGeno(char temp, char major)
    {
        if (temp == 0x00)
        {
            return -1;
        }
        if (temp == 0x01)
        {
            return 1;
        }
        if (temp == major)
        {
            return 0;
        }
        return 2;
    }
    
    private char getTempGeno(String s1, String s2)
    {
        if (s1.equals("0") || s2.equals("0"))
        {
            return 0x00;
        }
        if (!s1.equals(s2))
        {
            return 0x01;
        }
        return s1.charAt(0);
    }
    
    private PlinkPed(List<String> samples, Map<String,Map<Integer,Byte>> data,
            Map<String,String[]> meta, Map<Integer,Character> major, Map<Integer,Character> minor)
    {
        this.samples = samples;
        this.data = data;
        this.meta = meta;
        this.major = major;
        this.minor = minor;
    }
    
    /**
     * Get the genotypes as an array.  Missing data is coded as -1.
     * @return A 2D byte array of genotypes index by byte[sample][snp]
     */
    public byte[][] asArray()
    {
        byte[][] array = new byte[samples.size()][data.get(samples.get(0)).size()];
        
        int si = 0;
        for (String s: samples)
        {
            int snpi = 0;
            for (int i = 0; i < data.get(samples.get(0)).size(); i++)
            {
                array[si][i] = data.get(s).get(i);
            }
            si++;
        }
        
        return array;
    }
    
    /**
     * Get the genotypes as an array.  Missing data is coded as -1.
     * @return A 2D byte array of genotypes index by byte[snp][sample]
     */
    public byte[][] asArrayTransposed()
    {
        byte[][] array = new byte[data.get(samples.get(0)).size()][samples.size()];
        
        int si = 0;
        for (String s: samples)
        {
            for (int i = 0; i < data.get(samples.get(0)).size(); i++)
            {
                array[i][si] = data.get(s).get(i);
            }
            si++;
        }
        
        return array;
    }
    
    /**
     * Returns the samples associated with this Plink object
     * @return List of samples
     */
    public List<String> getSamples()
    {
        return samples;
    }
    
    /**
     * Returns the meta data associated with a sample
     * @param sample The sample to return meta data for
     * @return An array of strings representing the meta data in the order they
     * appeared in the file.
     */
    public String[] getMeta(String sample)
    {
        return meta.get(sample);
    }
    
    /**
     * Writes this object to file in Plink format
     * @param f File to write to
     * @throws IOException Thrown if there is a problem writing the file
     */
    public void writeToFile(File f) throws IOException
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        
        
        for (String sample: samples)
        {
            boolean first = true;
            for (String m: meta.get(sample))
            {
                if (!first)
                {
                    out.print(" ");
                }
                out.print(m);
                first = false;
            }
            Map<Integer,Byte> d = data.get(sample);
            for (int i = 0; i < d.size(); i++)
            {
                out.print(" ");
                out.print(plinkGeno(d.get(i),major.get(i),minor.get(i)));
            }
            out.println();
        }
        out.close();
    }
    
    private String plinkGeno(byte geno, Character major, Character minor)
    {
        switch (geno)
        {
            case 0:
                return major + " " + major;
            case 1:
                return major + " " + minor;
            case 2:
                return minor + " " + minor;
            case -1:
            default:
                return "0 0";
        }
    }
    
    /**
     * Writes this object to file in fastPhase format
     * @param f File to write to
     * @throws IOException Thrown if there is a problem writing the file
     */
    public void writeToFastPhase(File f) throws IOException
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        
        out.println(samples.size());
        out.println(data.get(samples.get(0)).size());
        
        byte[][] array = asArray();
        
        for (byte[] a: array)
        {
            StringBuilder line1 = new StringBuilder();
            StringBuilder line2 = new StringBuilder();
            
            for (byte g: a)
            {
                switch (g)
                {
                    case 0:
                        line1.append("0");
                        line2.append("0");
                        break;
                    case 1:
                        line1.append("0");
                        line2.append("1");
                        break;
                    case 2:
                        line1.append("1");
                        line2.append("1");
                        break;
                    default:
                        line1.append("?");
                        line2.append("?");
                        break;
                }
            }
            
            out.println(line1.toString());
            out.println(line2.toString());
        }
        
        out.close();
    }
    
    /**
     * Create a new instance similar to this one but with different genotype data
     * @param newData Byte array of the new data indexed by byte[sample][snp]
     * @return New PlinkNumeric instance with the new data
     */
    public PlinkPed changeData(byte[][] newData)
    {
        Map<String,Map<Integer,Byte>> nd = new HashMap<>();
        for (int i = 0; i < newData.length; i++)
        {
            String sample = samples.get(i);
            nd.put(sample, new HashMap<Integer, Byte>());
            byte[] n = newData[i];
            for (int j = 0; j < n.length; j++)
            {
                nd.get(sample).put(j,n[j]);
            }
        }
        
        return new PlinkPed(samples, nd, meta, major, minor);
    }
    
   
    private List<String> samples;
    
    //Map<Sample,Map<SNP,genotype>>
    private Map<String,Map<Integer,Byte>> data;
    
    //Map<Sample,Meta data for that sample>
    private Map<String,String[]> meta;
    
    private Map<Integer,Character> major;
    private Map<Integer,Character> minor;
}
