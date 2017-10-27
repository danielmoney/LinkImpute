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
package Files.VCFData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Position
{
    public Position(String s, Set<InfoDefinition> infodef, Set<FormatDefinition> formatdef)
    {
        this(s.split("\\s+"), infodef, formatdef);
    }
    
    public Position(String[] parts, Set<InfoDefinition> infodef, Set<FormatDefinition> formatdef)
    {
        chrom = parts[0];
        position = parts[1];
        id = parts[2];
        ref = parts[3];
        alt = parts[4].split(",");
        qual = parts[5];
        filter = parts[6];
        
        info = new HashMap<>();
        String[] infoparts = parts[7].split("\\;");
        for (String ip:infoparts)
        {
            String[] ipp = ip.split("=");
            String id = ipp[0];
            String val = null;
            if (ipp.length == 2)
            {
                val = ipp[1];
            }
            
            boolean found = false;
            for (InfoDefinition idef: infodef)
            {
                if (id.equals(idef.getID()))
                {
                    if (!idef.validate(id))
                    {
                        //throw error
                    }
                    info.put(idef,val);
                    found = true;
                }
            }
            if (!found)
            {
                //throw error
            }
        }
        
        format = new ArrayList<>();
        String[] formatparts = parts[8].split(":");
        for (String fp: formatparts)
        {
            boolean found = false;
            for (FormatDefinition fdef: formatdef)
            {
                if (fp.equals(fdef.getID()))
                {
                    format.add(fdef);
                    found = true;
                }
            }
            if (!found)
            {
               //throw error
            }
        }
    }
    
    public Position(String chrom, String position, String id, String ref, 
            String[] alt, String qual, String filter, 
            Map<InfoDefinition,String> info,
            List<FormatDefinition> format)
    {
        this.chrom = chrom;
        this.position = position;
        this.id = id;
        this.ref = ref;
        this.alt = alt;
        this.qual = qual;
        this.filter = filter;
        this.info = info;
        this.format = format;
    }
    
    public List<FormatDefinition> getFormat()
    {
        return format;
    }
    
    public String getChrom()
    {
        return chrom;
    }
    
    public void removeInfo(InfoDefinition id)
    {
        info.remove(id);
    }
    
    public String getPosition()
    {
        return position;
    }

    public void addFormat(FormatDefinition fd)
    {
        format.add(fd);
    }
    
    public void removeFormat(FormatDefinition fd)
    {
        format.remove(fd);
    }
    
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append(chrom); s.append("\t");
        s.append(position); s.append("\t");
        s.append(id); s.append("\t");
        s.append(ref); s.append("\t");
        for (String a: alt)
        {
            s.append(a); s.append(",");
        }
        s.deleteCharAt(s.length() - 1);
        s.append("\t");
        s.append(qual); s.append("\t");
        s.append(filter); s.append("\t");
        if (!info.isEmpty())
        {
            for (Entry<InfoDefinition,String> e: info.entrySet())
            {
                s.append(e.getKey().getID());
                s.append("=");
                s.append(e.getValue());
                s.append(";");
            }
        }
        else
        {
            s.append(".\t");
        }
        s.deleteCharAt(s.length() - 1);
        s.append("\t");
        
        for (FormatDefinition f: format)
        {
            s.append(f.getID());
            s.append(":");
        }
        s.deleteCharAt(s.length() - 1);
        
        return s.toString();
    }
    
    public void stripFormats()
    {
        format = new ArrayList<>();
    }
    
    public Position clone()
    {
        return new Position(chrom,position,id,ref,
                Arrays.copyOf(alt, alt.length),qual,filter,
                new HashMap<>(info),new ArrayList<>(format));
    }
    
    public boolean equals(Object o)
    {
        if (!(o instanceof Position))
        {
            return false;
        }
        Position p = (Position) o;
        return (chrom.equals(p.chrom) && position.equals(p.position));
    }
    
    public int hashCode()
    {
        return position.hashCode();
    }
    
    private String chrom;
    private String position;
    private String id;
    private String ref;
    private String[] alt;
    private String qual;
    private String filter;
    private Map<InfoDefinition,String> info;
    private List<FormatDefinition> format;
}
