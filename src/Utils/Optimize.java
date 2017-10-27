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

import java.util.Arrays;

/**
 * Optimizes integer parameter values.  There are probably better ways of doing
 * this!
 * @author Daniel Money
 */
public class Optimize
{
    /**
     * Create a new optimizer
     * @param value The "object" that is to be optimized
     * @param startmax The starting maximum values for each parameter
     * @param absmax The absolute maximum value for each parameter
     * @throws OptimizeException If there's an error!
     */    
    public Optimize(Value value, int[] startmax, int[] absmax) throws OptimizeException
    {
        this.value = value;
        this.startmax = startmax;
        this.absmax = absmax;
        bestV = 0.0;
        
        if (startmax.length != absmax.length)
        {
            throw new OptimizeException("startmax and absmax must be the same length");
        }
        
        optimize(0, new int[startmax.length]);
    }
    
    private double optimize(int pos, int[] vv) throws OptimizeException
    {
        int lbracket = 1;
        int ubracket = Math.max(3,Integer.highestOneBit(startmax[pos]-2) * 2 + 1);
        int mbracket = (lbracket + ubracket) / 2;

        double lvalue = value(pos,lbracket,vv);
        double uvalue = value(pos,ubracket,vv);
        double mvalue = value(pos,mbracket,vv);

        while ((uvalue > mvalue) && (mvalue > lvalue) && (ubracket <= absmax[pos]))
        {
            mbracket = ubracket;
            mvalue = uvalue;
            ubracket = (ubracket - 1) * 2 + 1;
            uvalue = value(pos,ubracket,vv);
        }
        
        while ((ubracket - mbracket) != 1)
        {
            int botbracket = (lbracket + mbracket) / 2;
            double botvalue = value(pos,botbracket,vv);

            int topbracket = (mbracket + ubracket) / 2;
            double topvalue = value(pos,topbracket,vv);

            if (lvalue >= botvalue)
            {
                ubracket = mbracket;
                uvalue = mvalue;

                mbracket = botbracket;
                mvalue = botvalue;

                continue;
            }

            if ((botvalue > lvalue) && (botvalue >= mvalue))
            {
                ubracket = mbracket;
                uvalue = mvalue;

                mbracket = botbracket;
                mvalue = botvalue;

                continue;
            }

            if ((mvalue > botvalue) && (mvalue >= topvalue))
            {
                lbracket = botbracket;
                lvalue = botvalue;

                ubracket = topbracket;
                uvalue = topvalue;

                continue;
            }

            if ((topvalue > mvalue) && (topvalue >= uvalue))
            {
                lbracket = mbracket;
                lvalue = mvalue;

                mbracket = topbracket;
                mvalue = topvalue;

                continue;
            }

            if (uvalue > topvalue)
            {
                lbracket = mbracket;
                lvalue = mvalue;

                mbracket = topbracket;
                mvalue = topvalue;

                continue;
            }
        }

        double ret = 0.0;
        if (lvalue >= mvalue)
        {
            ret = lvalue;
            vv[pos] = lbracket;
        }

        if ((lvalue < mvalue) && (mvalue >= uvalue))
        {
            ret = mvalue;
            vv[pos] = mbracket;
        }

        if (uvalue > mvalue)
        {
            ret = uvalue;
            vv[pos] = ubracket;
        }

        return ret;
    }
    
    private double value(int pos, int v, int[] vv) throws OptimizeException
    {
        if (v > absmax[pos])
        {
            return -Double.MAX_VALUE;
        }
        try
        {
            vv[pos] = v;
            if (pos == vv.length - 1)
            {
                double val = value.value(vv);
                if (val > bestV)
                {
                    bestV = val;
                    bestP = Arrays.copyOf(vv, vv.length);
                }
                return val;
            }
            else
            {
                return optimize(pos+1,vv);
            }
        }
        catch (Exception ex)
        {
            throw new OptimizeException(ex);
        }
        
    }
    
    /**
     * Returns the optimized value
     * @return The optimized value
     */
    public double getBestValue()
    {
        return bestV;
    }
    
    /**
     * Returns the optimized parameters
     * @return The optimized parameters
     */
    public int[] getBestParameter()
    {
        return bestP;
    }
    
    private Value value;
    private int[] startmax;
    private int[] absmax;
    
    private double bestV;
    private int[] bestP;
    
    /**
     * Optimization exception
     */
    public class OptimizeException extends Exception
    {

        /**
         * Constructor with a message
         * @param msg The message
         */
        public OptimizeException(String msg)
        {
            super(msg);
        }
        
        /**
         * Constructor with a causing exception
         * @param ex The causing exception
         */
        public OptimizeException(Exception ex)
        {
            super(ex);
        }
                
    }

}
