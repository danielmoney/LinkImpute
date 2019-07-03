package Utils;

/**
 * Interface for classes that show some sort of progress to standard out
 */
public interface Progress
{
    /**
     * Marks another task as done
     */
    public void done();

    /**
     * Marks multiple taskes as done
     * @param number The number to mark as done
     */
    public void done(int number);
}
