package Files.VCFMappers;

public interface Mapper<M>
{
    public M map(String v);
    public M[][] getArray(int dim1, int dim2);
}
