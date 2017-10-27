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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatDefinition<D>
{
    public FormatDefinition(String id, DataType type, String number, 
            String source, String version, String description)
    {
        this.source = source;
        this.version = version;
        this.description = description;
        this.number = number;
        this.id = id;
        this.type = type;
    }
    
    public FormatDefinition(String def)
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
                case "Source":
                    source = val;
                    break;
                case "Version":
                    version = val;
                case "Type":
                    type = DataType.valueOf(val.toUpperCase(Locale.ENGLISH));
                    if (type == DataType.FLAG)
                    {
                        //Throw exception as this isn't allowed.
                    }
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
        if (source != null)
        {
            s.append(",Source=");
            s.append(source);            
        }
        if (version != null)
        {
            s.append(",Version=");
            s.append(version);            
        }
        s.append(">");
        return s.toString();
    }
    
    private String source;
    private String version;
    private String description;
    private String number;
    private String id;    
    private DataType type;
    
    private static final Pattern pat = Pattern.compile("(?<=[<,])(.*?)=(.*?)(?=[,>])");
}
