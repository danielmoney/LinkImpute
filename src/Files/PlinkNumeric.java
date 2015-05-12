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

/**
 * Class for dealing with data in the plink .raw format.  This data is generated
 * using the -recodeA option of Plink
 * @author Daniel Money
 */
public class PlinkNumeric
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
    public PlinkNumeric(File f) throws IOException, InvalidGenotypeException,
            RepeatedSNPException, RepeatedSampleException, WrongNumberOfSNPsException
    {
        this(f,6,false);
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
    public PlinkNumeric(File f, int metacolumns) throws IOException, InvalidGenotypeException,
            RepeatedSNPException, RepeatedSampleException, WrongNumberOfSNPsException
    {
        this(f,6,false);
    }
    
    /**
     * Constructor.  Loads data from a file.  Allows for the removal of the
     * last two characters from SNP names - useful when _base is included after
     * the snp name to indicate the major allele base.
     * @param f File to load data from
     * @param baseincluded If true removes the last two characters from snp names
     * @throws IOException If there is a problem reading from the file
     * @throws InvalidGenotypeException If there is an invalid genotype in the file
     * @throws RepeatedSNPException If a SNP name is repeated
     * @throws RepeatedSampleException If a Sample name is repeated
     * @throws WrongNumberOfSNPsException If a sample has the wrong number of SNPs
     */
    public PlinkNumeric(File f, boolean baseincluded) throws IOException, InvalidGenotypeException,
            RepeatedSNPException, RepeatedSampleException, WrongNumberOfSNPsException
    {
        this(f,6,baseincluded);
    }
    
    /**
     * Constructor.  Loads data from a file.
     * @param f File to load data from
     * @param metacolumns The number of meta columns before genotype data begins
     * @param baseincluded If true removes the last two characters from snp names
     * @throws IOException If there is a problem reading from the file
     * @throws InvalidGenotypeException If there is an invalid genotype in the file
     * @throws RepeatedSNPException If a SNP name is repeated
     * @throws RepeatedSampleException If a Sample name is repeated
     * @throws WrongNumberOfSNPsException If a sample has the wrong number of SNPs
     */
    public PlinkNumeric(File f, int metacolumns, boolean baseincluded) throws IOException, InvalidGenotypeException,
            RepeatedSNPException, RepeatedSampleException, WrongNumberOfSNPsException
    {
        BufferedReader in = new BufferedReader(new FileReader(f));
         
        String line = in.readLine();
        String[] parts = line.split("\\s");

        SNPs = new ArrayList<>(parts.length - metacolumns);
        samples = new ArrayList<>();
        data = new HashMap<>();
        meta = new HashMap<>();

        metahead = Arrays.copyOf(parts, metacolumns);
        for (int i = metacolumns; i < parts.length; i++)
        {
            String ns;
            if (baseincluded)
            {
                ns = parts[i].substring(0, parts[i].length() - 2);
            }
            else
            {
                ns = parts[i];
            }
            if (SNPs.contains(ns))
            {
                throw new RepeatedSNPException(ns);
            }
            else
            {
                SNPs.add(ns);
            }
        }

        while ((line = in.readLine()) != null)
        {
            parts = line.split("\\s");

            if (parts.length != (metacolumns + SNPs.size()))
            {
                throw new WrongNumberOfSNPsException(parts[0]);
            }

            String s = parts[0];
            if (samples.contains(s))
            {
                throw new RepeatedSampleException(s);
            }
            else
            {
                samples.add(s);
            }

            meta.put(s, Arrays.copyOf(parts, metacolumns));

            Map<String,Byte> d = new HashMap<>();
            for (int i = metacolumns; i < parts.length; i++)
            {
                d.put(SNPs.get(i-metacolumns),getValue(parts[i]));
            }
            data.put(s,d);
        }

        in.close();
    }
    
    private PlinkNumeric(List<String> SNPs, List<String> samples, Map<String,Map<String,Byte>> data,
            String[] metahead, Map<String,String[]> meta)
    {
        this.SNPs = SNPs;
        this.samples = samples;
        this.data = data;
        this.metahead = metahead;
        this.meta = meta;
    }
    
    /**
     * Get the genotypes as an array.  Missing data is coded as -1.
     * @return A 2D byte array of genotypes index by byte[sample][snp]
     */
    public byte[][] asArray()
    {
        byte[][] array = new byte[samples.size()][SNPs.size()];
        
        int si = 0;
        for (String s: samples)
        {
            int snpi = 0;
            for (String snp: SNPs)
            {
                array[si][snpi] = data.get(s).get(snp);
                snpi++;
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
        byte[][] array = new byte[SNPs.size()][samples.size()];
        
        int si = 0;
        for (String s: samples)
        {
            int snpi = 0;
            for (String snp: SNPs)
            {
                array[snpi][si] = data.get(s).get(snp);
                snpi++;
            }
            si++;
        }
        
        return array;
    }
    
    /**
     * Returns the SNPs associated with this Plink object
     * @return List of SNPs
     */
    public List<String> getSNPs()
    {
        return SNPs;
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
     * Returns the meta header data - i.e. the column headings of the meta data
     * @return An array of strings representing the meta header data in the order
     * they appeared in the file.
     */
    public String[] getMetaHead()
    {
        return metahead;
    }
    
    private byte getValue(String s) throws InvalidGenotypeException
    {
        if (s.equals("NA"))
        {
            return -1;
        }
        else
        {
            try
            {
                Byte b = Byte.parseByte(s);
                if ((b < 0) || (b > 2))
                {
                    throw new InvalidGenotypeException(s);
                }
                return b;
            }
            catch (NumberFormatException e)
            {
                throw new InvalidGenotypeException(s);
            }
        }
    }
    
    /**
     * Writes this object to file in Plink format
     * @param f File to write to
     * @throws IOException Thrown if there is a problem writing the file
     */
    public void writeToFile(File f) throws IOException
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        
        boolean first = true;
        for (String mh: metahead)
        {
            if (!first)
            {
                out.print(" ");
            }
            out.print(mh);
            first = false;
            
        }
        for (String s: SNPs)
        {
            out.print(" ");
            out.print(s);
        }
        out.println();
        
        for (String sample: samples)
        {
            first = true;
            for (String m: meta.get(sample))
            {
                if (!first)
                {
                    out.print(" ");
                }
                out.print(m);
                first = false;
            }
            Map<String,Byte> d = data.get(sample);
            for (String snp : SNPs)
            {
                out.print(" ");
                if (d.get(snp) != -1)
                {
                    out.print(d.get(snp));
                }
                else
                {
                    out.print("NA");
                }
            }
            out.println();
        }
        out.close();
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
        out.println(SNPs.size());
        
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
    public PlinkNumeric changeData(byte[][] newData)
    {
        Map<String,Map<String,Byte>> nd = new HashMap<>();
        for (int i = 0; i < newData.length; i++)
        {
            String sample = samples.get(i);
            nd.put(sample, new HashMap<String, Byte>());
            byte[] n = newData[i];
            for (int j = 0; j < n.length; j++)
            {
                nd.get(sample).put(SNPs.get(j),n[j]);
            }
        }
        
        return new PlinkNumeric(SNPs, samples, nd, metahead, meta);
    }
    
    /**
     * Creates a new PlinkNumeric instance with the same meta information as this
     * instance but different data
     * @param newData Byte array of the new genotype data indexed by byte[sample][snp]
     * @param samples The new list of samples (in the order they appear in newData)
     * @param SNPs The new list of SNPs (in the order they appear in newData)
     * @return New PlinkNumeric instance with the new data
     */
    public PlinkNumeric changeData(byte[][] newData, List<String> samples, List<String> SNPs)
    {
        Map<String,Map<String,Byte>> nd = new HashMap<>();
        for (int i = 0; i < newData.length; i++)
        {
            String sample = samples.get(i);
            nd.put(sample, new HashMap<String, Byte>());
            byte[] n = newData[i];
            for (int j = 0; j < n.length; j++)
            {
                nd.get(sample).put(SNPs.get(j),n[j]);
            }
        }
        
        return new PlinkNumeric(SNPs, samples, nd, metahead, meta);
    }
    
    private List<String> SNPs;
    private List<String> samples;
    
    //Map<Sample,Map<SNP,genotype>>
    private Map<String,Map<String,Byte>> data;
    
    //Map<Sample,Meta data for that sample>
    private Map<String,String[]> meta;
    private String[] metahead;
}
