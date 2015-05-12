package Utils;

public class Progress
{
    public Progress(int total)
    {
        shown = 0;
        done = 0;
        this.total = total;
        print(0);
    }
    
    public synchronized void done()
    {
        done(1);
    }
    
    public synchronized void done(int number)
    {
        done += number;
        int show = (int) Math.floor((double) size * (double) done / (double) total);
        if (show != shown)
        {
            print(show);
            shown = show;
        }
        if (done == total)
        {
            print(size);
            System.out.println();
        }
    }
    
    private void print(int show)
    {
        System.out.print("\r\t[");
        for (int i = 0; i < show; i++)
        {
            System.out.print("=");
        }
        for (int i = show; i < size; i++)
        {
            System.out.print(" ");
        }
        System.out.print("]");
    }
    
    private int shown;
    private int done;
    private final int total;
    private final static int size = 50;
}
