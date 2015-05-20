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

package Utils;

import java.nio.ByteBuffer;

/**
 * Wrapper around ByteBuffer to make using it easier.  Was written because
 * Java array bound checks were taking a long time and using this made the code
 * run quicker.  Not sure it's still necessary as I suspect the code may run
 * fine without it but it's now a significant amount of effort to remove.
 * @author Daniel Money
 */
public class BufferByteArray2D
{

    /**
     * Create a new BufferByteArray contains the same information as an array
     * @param a A byte array to copy the information from
     */
    public BufferByteArray2D(byte[][] a)
    {
        outersize = a.length;
        innersize = a[0].length;
        buffer = ByteBuffer.allocateDirect(innersize * outersize);
        //buffer = ByteBuffer.allocate(size);
        for (int i = 0; i < a.length; i++)
        {
            byte[] ai = a[i];
            if (ai.length != innersize)
            {
                //ERROR
            }
            for (int j = 0; j < ai.length; j++)
            {
                buffer.put(i * innersize + j, ai[j]);
            }
        }
    }
    
    /**
     * Create a new BufferByteArray of the given size
     * @param size Size of the new BufferByteArray
     */
    public BufferByteArray2D(int outersize, int innersize)
    {
        this.outersize = outersize;
        this.innersize = innersize;
        buffer = ByteBuffer.allocateDirect(innersize * outersize);
    }
    
    private BufferByteArray2D(ByteBuffer buffer, int outersize, int innersize)
    {
        this.buffer = buffer;
        this.outersize = outersize;
        this.innersize = innersize;
    }
    
    /**
     * Get the value at a given position
     * @param pos The position to get the value for
     * @return The byte value at that position
     */
    public byte get(int outerpos, int innerpos)
    {
        if (outerpos < 0 || outerpos >= outersize || innerpos < 0 || innerpos >= innersize)
        {
            throw new IndexOutOfBoundsException("Tried to access position: " + outerpos + ", " + innerpos +
                    ". Size of BufferByteArray: " + outersize + ", " + innersize);
            //return (byte) 0;
        }
        return buffer.get(outerpos * innersize + innerpos);
    }

    /**
     * Sets the value at a given position
     * @param pos The position to set the value for
     * @param val The value to set the position to
     */
    public void set(int outerpos, int innerpos, byte val)
    {
        if (outerpos < 0 || outerpos >= outersize || innerpos < 0 || innerpos >= innersize)
        {
            throw new IndexOutOfBoundsException("Tried to access position: " + outerpos + ", " + innerpos +
                    ". Size of BufferByteArray: " + outersize + ", " + innersize);
        }
        buffer.put(outerpos * innersize + innerpos, val);
    }
    
    /**
     * Returns the size of the BufferByteArray
     * @return The size
     */
    public int innersize()
    {
        return innersize;
    }
    
    public int outersize()
    {
        return outersize;
    }
    
    public int size()
    {
        return innersize * outersize;
    }
    
    public BufferByteArray2D clone()
    {
        ByteBuffer nb = ByteBuffer.allocateDirect(innersize * outersize);
        for (int i = 0; i < innersize * outersize; i++)
        {
            nb.put(i,buffer.get(i));
        }
        return new BufferByteArray2D(nb,outersize,innersize);
    }
    
    private final int outersize;
    private final int innersize;
    private final ByteBuffer buffer;
}
