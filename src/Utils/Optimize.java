package Utils;

import java.util.Arrays;

public class Optimize
{
    /*public Optimize(Value v, int startmax) throws Exception
    {
        int lbracket = 1;
        int ubracket = Math.max(3,Integer.highestOneBit(startmax-2) * 2 + 1);
        int mbracket = (lbracket + ubracket) / 2;
        
        double lvalue = v.value(lbracket);
        double uvalue = v.value(ubracket);
        double mvalue = v.value(mbracket);
        
        while ((uvalue > mvalue) && (mvalue > lvalue))
        {
            mbracket = ubracket;
            mvalue = uvalue;
            ubracket = (ubracket - 1) * 2 + 1;
            uvalue = v.value(ubracket);
        }
        
        while ((ubracket - mbracket) != 1)
        {
            int botbracket = (lbracket + mbracket) / 2;
            double botvalue = v.value(botbracket);
            
            int topbracket = (mbracket + ubracket) / 2;
            double topvalue = v.value(topbracket);
            
            if (lvalue >= botvalue)
            {
                ubracket = mbracket;
                uvalue = mvalue;
                
                mbracket = botbracket;
                mvalue = botvalue;
                
                continue;
            }
            
            if ((botvalue >= lvalue) && (botvalue > mvalue))
            {
                ubracket = mbracket;
                uvalue = mvalue;
                
                mbracket = botbracket;
                mvalue = botvalue;
                
                continue;
            }
            
            if ((mvalue >= botvalue) && (mvalue > topvalue))
            {
                lbracket = botbracket;
                lvalue = botvalue;
                
                ubracket = topbracket;
                uvalue = topvalue;
                
                continue;
            }
            
            if ((topvalue >= mvalue) && (topvalue > uvalue))
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
        
        if (lvalue >= mvalue)
        {
            value = lvalue;
            p = lbracket;
        }
        
        if ((lvalue < mvalue) && (mvalue >= uvalue))
        {
            value = mvalue;
            p = mbracket;
        }
        
        if (uvalue > mvalue)
        {
            value = uvalue;
            p = ubracket;
        }
    }*/
    
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

        while ((uvalue > mvalue) && (mvalue > lvalue) && (ubracket >= absmax[pos]))
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
    
    public double getBestValue()
    {
        return bestV;
    }
    
    public int[] getBestParameter()
    {
        return bestP;
    }
    
    private Value value;
    private int[] startmax;
    private int[] absmax;
    
    private double bestV;
    private int[] bestP;
    
    public class OptimizeException extends Exception
    {
        public OptimizeException(String msg)
        {
            super(msg);
        }
        
        public OptimizeException(Exception ex)
        {
            super(ex);
        }
                
    }

}
