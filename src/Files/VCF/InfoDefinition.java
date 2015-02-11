package Files.VCF;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoDefinition
{
    public InfoDefinition(String id, DataType type, String description, String number)
    {
        this.description = description;
        this.number = number;
        this.id = id;
        this.type=type;
    }
    
    public InfoDefinition(String def)
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
                case "Number":
                    number = val;
                    break;
                case "Description":
                    description = val;
                    break;
                case "Type":
                    type = DataType.valueOf(val.toUpperCase(Locale.ENGLISH));
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
        s.append(",Number=");
        s.append(number);
        s.append(",Type=");
        s.append(type);
        s.append(",Description=");
        s.append(description);
        s.append(">");
        return s.toString();
    }
    
    private String description;
    private String number;
    private String id; 
    private DataType type;
    
    private static final Pattern pat = Pattern.compile("(?<=[<,])(.*?)=(.*?)(?=[,>])");
}
