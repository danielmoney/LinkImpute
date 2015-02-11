package Files.VCF;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterDefinition<D>
{
    public FilterDefinition(String id, String description)
    {
        this.description = description;
        this.id = id;
    }
    
    public FilterDefinition(String def)
    {
        Matcher match = pat.matcher(def);
        
        while (match.find())
        {
            String name = match.group(1);
            String val = match.group(2);
            
            switch (name)
            {
                case "ID":
                    id = val;
                    break;
                case "Description":
                    description = val;
                    break;
                default:
                    //Throw Exception
            }
        }
    }
    
    
    public String getID()
    {
        return id;
    }
    
    public boolean validate(String val)
    {
        return true;
    }
    
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append("<ID=");
        s.append(id);
        s.append(",Description=");
        s.append(description);
        s.append(">");
        return s.toString();
    }
    
    private String description;
    private String id;    
    
    private static final Pattern pat = Pattern.compile("(?<=[<,])(.*?)=(.*?)(?=[,>])");
}
