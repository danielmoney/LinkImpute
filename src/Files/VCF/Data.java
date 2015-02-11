package Files.VCF;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Data
{
    public Data(List<String> samples)
    {
        this.samples = new ArrayList<>(samples);
        data = new LinkedHashMap<>();
        positions = new ArrayList<>();
    }
    
    public Data(List<Position> positions, List<String> samples, Map<Position,byte[][]> data)
    {
        this.positions = positions;
        this.samples = new ArrayList<>(samples);
        this.data = data;
    }
    
    public void addPosition(String data, Set<InfoDefinition> infodef, Set<FormatDefinition> formatdef)
    {
        String[] parts = data.split("\\s+",10);
        addPosition(new Position(Arrays.copyOfRange(parts, 0,9), infodef, formatdef), parts[9].split("\\s+"));
    }
    
    public void addPosition(Position position, String[] posdata)
    {
        //String[] parts = posdata.split("\\s+");
        //int i = 0;
        //Map<String,Formats> d = new HashMap<>();
        //for (String s: samples)
        //{
        //    d.put(s, new Formats(position.getFormat(),parts[i]));
        //   i++;
        //}
        
        //positions.add(position);
        //data.put(position,d);*/
        
        
        byte[][] d = new byte[posdata.length][];
        for (int i = 0; i < posdata.length; i++)
        {
            d[i] = posdata[i].getBytes();
        }
        data.put(position, d);
        positions.add(position);
    }
    
    public Position getPosition(String chrom, String id)
    {
        for (Position p: positions)
        {
            if (p.getChrom().equals(chrom) && p.getPosition().equals(id))
            {
                return p;
            }
        }
        //Should  throw error but for now..
        return null;
    }
    
    /*public Formats getSingleFormats(Position position, String sample)
    {
        return data.get(position).get(sample);
    }
    
    public Map<String,Formats> getFormatsByPosition(Position position)
    {
        return data.get(position);
    }
    
    public Map<Position,Formats> getFormatsBySample(String sample)
    {
        HashMap<Position,Formats> ret = new HashMap<>();
        
        for (Entry<Position,Map<String,Formats>> e: data.entrySet())
        {
            ret.put(e.getKey(), e.getValue().get(sample));
        }
        
        return ret;
    }*/
    
    public byte[][] asArray(FormatDefinition f, Map<String,Byte> map)
    {
        byte[][] array = new byte[samples.size()][data.size()];
        
        int pi = 0;
        for (Position p: positions)
        {
            int si = 0;
            byte[][] pdata = data.get(p);
            for (int i=0; i < pdata.length; i++)
            {
                array[si][pi] = map.get(retrieve(pdata[i], p.getFormat(), f));
                si++;
            }
            pi++;
        }
        
        return array;
    }
    
    public byte[][] asArrayTransposed(FormatDefinition f, Map<String,Byte> map)
    {
        byte[][] array = new byte[data.size()][samples.size()];
        
        int pi = 0;
        for (Position p: positions)
        {
            int si = 0;
            byte[][] pdata = data.get(p);
            for (int i=0; i < pdata.length; i++)
            {
                array[pi][si] = map.get(retrieve(pdata[i], p.getFormat(), f));
                si++;
            }
            pi++;
        }
        
        return array;
    }
    
    public void removePosition(Position p)
    {
        positions.remove(p);
        data.remove(p);
    }
    
    public void removeInfo(InfoDefinition id)
    {
        for (Position p : positions)
        {
            p.removeInfo(id);
        }
    }
    
    public void addFormat(FormatDefinition fd, Position p, String[] d)
    {
        p.addFormat(fd);
        byte[][] old = data.get(p);
        for (int i = 0; i < old.length; i++)
        {
            old[i] = add(old[i], d[i]);
        }
    }
    
    public void changeFormat(FormatDefinition fd, Position p, String[] d)
    {
        byte[][] old = data.get(p);
        for (int i = 0; i < old.length; i++)
        {
            old[i] = change(old[i], p.getFormat(), fd, d[i]);
        }
    }

    
    public void removeFormat(FormatDefinition fd)
    {
        for (Position p : positions)
        {
            removeFormat(p,fd);
        }
    }
    
    public void removeFormat(Position p, FormatDefinition fd)
    {
        byte[][] d = data.get(p);
        for (int i = 0; i < d.length; i++)
        {
            d[i] = remove(d[i],p.getFormat(),fd);
        }
        p.removeFormat(fd);
    }
    
    public void removeSample(String s)
    {
        int pos = samples.indexOf(s);
        for (Entry<Position,byte[][]> e: data.entrySet())
        {
            byte[][] o = e.getValue();
            byte[][] n = new byte[o.length-1][];
            for (int i = 0; i < o.length; i++)
            {
                if (i < pos)
                {
                    n[i] = o[i];
                }
                if (i > pos)
                {
                    n[i - 1] = o[i];
                }
                    
            }
            data.put(e.getKey(),n);
        }
        samples.remove(pos);
    }
    
    public void toStream(PrintStream out) throws IOException
    {
        StringBuilder temp = new StringBuilder();
        /*out.print("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT");
        for (String s: samples)
        {
            out.print("\t");
            out.print(s);
        }
        out.print("\n");*/
        temp.append("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT");
        for (String s: samples)
        {
            temp.append("\t");
            temp.append(s);
        }
        temp.append("\n");
        out.print(temp);
        
        /*for (Entry<Position,byte[][]> e: data.entrySet())
        {
            out.print(e.getKey());
            for (byte[] s: e.getValue())
            {
                out.print("\t");
                out.write(s);
            }
            out.println();
        }*/
        
        for (Entry<Position,byte[][]> e: data.entrySet())
        {
            temp = new StringBuilder();
            temp.append(e.getKey());
            for (byte[] s: e.getValue())
            {
                temp.append("\t");
                temp.append(new String(s));
            }
            temp.append("\n");
            out.print(temp);
        }
    }
    
    public int count()
    {
        return positions.size() * samples.size();
    }
    
    public int countIf(FormatDefinition d, String regex)
    {
        Pattern pattern = Pattern.compile(regex);
        int c = 0;
        
        for (Position p: positions)
        {
            for (byte[] s: data.get(p))
            {
                Matcher m = pattern.matcher(retrieve(s,p.getFormat(),d));
                if (m.find())
                {
                    c++;
                }
            }
        }
        
        return c;
    }
    
    public List<Position> getPositions()
    {
        return positions;
    }
    
    public List<String> getSamples()
    {
        return samples;
    }
    
    public String getValue(Position p, int s, FormatDefinition f)
    {
        return retrieve(data.get(p)[s], p.getFormat(), f);
    }
    
    public String getValue(Position p, String s, FormatDefinition f)
    {
        return retrieve(data.get(p)[samples.indexOf(s)], p.getFormat(), f);
    }
    
    private static String retrieve(byte[] s, List<FormatDefinition> defs, FormatDefinition wanted)
    {
        return retrieve(new String(s), defs, wanted);
    }
    
    public static String retrieve(String s, List<FormatDefinition> defs, FormatDefinition wanted)
    {
        Pattern p = Pattern.compile("(?:\\S+?:){" + defs.indexOf(wanted) + "}(\\S+?)(?::|$)");
        Matcher m = p.matcher(s);
        m.find();
        return m.group(1);
    }
    
    private static byte[] remove(byte[] s, List<FormatDefinition> defs, FormatDefinition remove)
    {
        return remove(new String(s), defs, remove).getBytes();
    }
    
    public static String remove(String s, List<FormatDefinition> defs, FormatDefinition remove)
    {
        int rp = defs.indexOf(remove);
        StringBuilder ns = new StringBuilder();
        String[] split = s.split(":");
        for (int i = 0; i < split.length; i++)
        {
            if (i != rp)
            {
                ns.append(split[i]);
                if (!((i == split.length - 1) ||
                        ((i == split.length - 2) && (rp == split.length - 1)) ))
                {
                    ns.append(":");
                }
            }
        }
        return ns.toString();
    }
    
    private byte[] add(byte[] o, String n)
    {
        return add(new String(o), n).getBytes();
    }
    
    public String add(String o, String n)
    {
        return o + ":" + n;
    }
    
    private byte[] change(byte[] s, List<FormatDefinition> defs, 
            FormatDefinition change, String value)
    {
        return change(new String(s), defs, change, value).getBytes();
    }
    
    private String change(String s, List<FormatDefinition> defs, 
            FormatDefinition change, String value)
    {
        int rp = defs.indexOf(change);
        StringBuilder ns = new StringBuilder();
        String[] split = s.split(":");
        for (int i = 0; i < split.length; i++)
        {
            if (i != rp)
            {
                ns.append(split[i]);
            }
            else
            {
                ns.append(value);
            }
            if (i != (split.length - 1)) 
            {
                ns.append(":");
            }
                
        }
        return ns.toString();
    }

    
    public Data clone()
    {
        Map<Position,byte[][]> ndata = new HashMap<>();
        List<Position> np = new ArrayList<>();
        for (Entry<Position,byte[][]> e: data.entrySet())
        {
            Position npp = e.getKey().clone();
            ndata.put(npp,deepCopy(e.getValue()));
            np.add(npp);
        }
        return new Data(np, new ArrayList<String>(samples), ndata);
    }
    
    private byte[][] deepCopy(byte[][] orig)
    {
        byte[][] n = new byte[orig.length][];
        for (int i = 0; i < orig.length; i++)
        {
            n[i] = Arrays.copyOf(orig[i], orig[i].length);
        }
        return n;
    }
            
    
    private List<Position> positions;
    private ArrayList<String> samples;
    //private Map<Position,Map<String,Formats>> data;
    //private Map<Position,Map<String,char[]>> data;
    private Map<Position,byte[][]> data;
}
