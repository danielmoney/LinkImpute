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

import Files.VCFData.Data;
import Files.VCFData.DataType;
import Files.VCFData.FilterDefinition;
import Files.VCFData.FormatDefinition;
import Files.VCFData.InfoDefinition;
import Files.VCFData.Meta;
import Files.VCFData.Position;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VCF
{
    /****************************************************************
     * VCF code is all still very developmental and not documented! *
     * Use at your own risk - it may not work as expected.          *
     ****************************************************************/
    
    public VCF(File f) throws FileNotFoundException, IOException
    {
        BufferedReader in;
        try
        {
            in = new BufferedReader(new FileReader(f));
        }
        catch (FileNotFoundException e)
        {
            throw e;
            //throw exception or just use that one, who knows!
        }
        
        try
        {
            boolean readHeader = false;
            Set<String> other = new HashSet<>();
            Set<FilterDefinition> filter = new HashSet<>();
            Set<InfoDefinition> info = new HashSet<>();
            Set<FormatDefinition> format = new HashSet<>();
            
            String line;
            while ((line = in.readLine()) != null)
            {
                if (line.startsWith("##"))
                {
                    if (readHeader)
                    {
                        //throw error as we should already by done with meta
                    }
                    boolean special = false;
                    String removed = line.substring(2);
                    if (removed.startsWith("FORMAT"))
                    {
                        format.add(new FormatDefinition(removed));
                        special = true;
                    }
                    if (removed.startsWith("INFO"))
                    {
                        info.add(new InfoDefinition(removed));
                        special = true;
                    }
                    if (removed.startsWith("FILTER"))
                    {
                        filter.add(new FilterDefinition(removed));
                        special = true;
                    }
                    if (!special)
                    {
                        other.add(removed);
                    }
                }
                else if (line.startsWith("#"))
                {
                    //Header Stuff
                    String[] parts = line.split("\\s+");
                    data = new Data(Arrays.asList(Arrays.copyOfRange(parts, 9, parts.length)));
                    
                    readHeader = true;
                    meta = new Meta(info,format,filter,other);
                }
                else
                {
                    data.addPosition(line,info,format);
                    //Data Stuff
                }
            }
        }
        catch (IOException e)
        {
            //throw exception or just use that one, who knows!
            throw e;
        }
    }
    
    public VCF(Meta meta, Data data)
    {
        this.meta = meta;
        this.data = data;
    }
    
    public void writeFile(File f) throws FileNotFoundException, IOException
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
       
        meta.toWriter(out);
        data.toWriter(out);

        out.close();
    }
    
    public Meta getMeta()
    {
        return meta;
    }
    
    public Data getData()
    {
        return data;
    }
    
        
    public void stripInfo()
    {
        for (InfoDefinition id: new HashSet<>(meta.getInfoDefinitions()))
        {
            removeInfo(id);
        }
    }
    
    public void removeInfo(String s)
    {
        removeInfo(meta.getInfoDefintion(s));
    }
    
    public void removeInfo(InfoDefinition id)
    {
        data.removeInfo(id);
        meta.removeInfoDefinition(id);
    }
    
    public void removeFormat(String s)
    {
        removeFormat(meta.getFormatDefintion(s));
    }
    
    public void removeFormat(FormatDefinition fd)
    {
        data.removeFormat(fd);
        meta.removeFormatDefiniton(fd);
    }
    
    public VCF clone()
    {
        return new VCF(meta.clone(), data.clone());
    }
    
    public static VCF fromDoubleArray(double[][] values, 
            List<Position> positions, List<String> samples, 
            String id, String description)
    {
        FormatDefinition fd = new FormatDefinition(id, DataType.FLOAT, "1",
            null, null, description);
        
        Set<FormatDefinition> formats = new HashSet<>();
        formats.add(fd);
        Set<String> other = new HashSet<>();
        other.add("fileformat=VCFv4.1");
        Meta m = new Meta(new HashSet<InfoDefinition>(), formats, 
                new HashSet<FilterDefinition>(), other);
        
        Data d = new Data(samples);
        for (int i = 0; i < values.length; i++)
        {
            double[] v = values[i];
            String[] sv = new String[v.length];
            for (int j = 0; j < v.length; j++)
            {
                sv[j] = Double.toString(v[j]);
            }
            Position np = positions.get(i).clone();
            np.stripFormats();
            np.addFormat(fd);
            d.addPosition(np, sv);
        }
        return new VCF(m,d);
    }

    private Meta meta;
    private Data data;
}
