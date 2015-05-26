package Files.VCFData;

public enum DataType
{
    INTEGER,
    FLOAT,
    FLAG,
    CHARACTER,
    STRING;
    
    public String toString()
    {
        switch (this)
        {
            case INTEGER:
                return "Integer";
            case FLOAT:
                return "Float";
            case CHARACTER:
                return "Character";
            case FLAG:
                return "Flag";
            case STRING:
                return "String";
            default:
                throw new IllegalArgumentException();
        }
    }
}
