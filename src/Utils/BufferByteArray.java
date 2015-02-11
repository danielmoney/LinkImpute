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
public class BufferByteArray
{

    /**
     * Create a new BufferByteArray contains the same information as an array
     * @param a A byte array to copy the information from
     */
    public BufferByteArray(byte[] a)
    {
        this.size = a.length;
        buffer = ByteBuffer.allocateDirect(size);
        //buffer = ByteBuffer.allocate(size);
        for (int i = 0; i < a.length; i++)
        {
            buffer.put(i, a[i]);
        }
    }
    
    /**
     * Create a new BufferByteArray of the given size
     * @param size Size of the new BufferByteArray
     */
    public BufferByteArray(int size)
    {
        this.size = size;
        buffer = ByteBuffer.allocateDirect(size);
    }
    
    private BufferByteArray(ByteBuffer buffer, int size)
    {
        this.buffer = buffer;
        this.size = size;
    }
    
    /**
     * Get the value at a given position
     * @param pos The position to get the value for
     * @return The byte value at that position
     */
    public byte get(int pos)
    {
        if (pos < 0 || pos >= size)
        {
            throw new IndexOutOfBoundsException("Tried to access position: " + pos +
                    ". Size of BufferByteArray: " + size);
            //return (byte) 0;
        }
        return buffer.get(pos);
    }

    /**
     * Sets the value at a given position
     * @param pos The position to set the value for
     * @param val The value to set the position to
     */
    public void set(int pos, byte val)
    {
        if (pos < 0 || pos >= size)
        {
            throw new IndexOutOfBoundsException("Tried to access position: " + pos +
                    ". Size of BufferByteArray: " + size);
        }
        buffer.put(pos, val);
    }
    
    /**
     * Returns the size of the BufferByteArray
     * @return The size
     */
    public int size()
    {
        return size;
    }
    
    public BufferByteArray clone()
    {
        ByteBuffer nb = ByteBuffer.allocateDirect(size);
        for (int i = 0; i < size; i++)
        {
            nb.put(i,buffer.get(i));
        }
        return new BufferByteArray(nb,size);
    }
    
    private final int size;
    private final ByteBuffer buffer;
}
