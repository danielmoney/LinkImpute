package Files.VCF;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class Meta
{
    public Meta(Set<InfoDefinition> infodef, Set<FormatDefinition> formatdef,
            Set<FilterDefinition> filters, Set<String> other)
    {
        this.infodef = infodef;
        this.formatdef = formatdef;
        this.filters = filters;
        this.other = other;
    }
    
    public void toWriter(PrintWriter out)
    {
        for (String o: other)
        {
            out.print("##");
            out.print(o);
            out.print("\n");
        }
        
        for (InfoDefinition i: infodef)
        {
            out.print("##INFO=");
            out.print(i);
            out.print("\n");
        }
        
        for (FormatDefinition f: formatdef)
        {
            out.print("##FORMAT=");
            out.print(f);
            out.print("\n");
        }
       
        for (FilterDefinition f: filters)
        {
            out.print("##FILTER=");
            out.print(f);
            out.print("\n");
        }        
    }
    
    public FormatDefinition getFormatDefintion(String s)
    {
        for (FormatDefinition f: formatdef)
        {
            if (f.getID().equals(s))
            {
                return f;
            }
        }
        //Really throw exception but for now
        return null;
    }
    
    public void addFormatDefinition(FormatDefinition f)
    {
        formatdef.add(f);
    }
    
    public Set<InfoDefinition> getInfoDefinitions()
    {
        return infodef;
    }
    
    public InfoDefinition getInfoDefintion(String s)
    {
        for (InfoDefinition i: infodef)
        {
            if (i.getID().equals(s))
            {
                return i;
            }
        }
        //Really throw exception but for now
        return null;
    }
    
    public void removeInfoDefinition(InfoDefinition id)
    {
        infodef.remove(id);
    }
    
    public void removeFormatDefiniton(FormatDefinition fd)
    {
        formatdef.remove(fd);
    }
    
    public Meta clone()
    {
        return new Meta(new HashSet<>(infodef),
                new HashSet<>(formatdef),
                new HashSet<>(filters),
                new HashSet<>(other));
    }
    
    private Set<FilterDefinition>  filters;
    private Set<InfoDefinition> infodef;
    private Set<FormatDefinition> formatdef;
    private Set<String> other;
}
