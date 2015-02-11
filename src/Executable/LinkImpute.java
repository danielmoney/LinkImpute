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

package Executable;

import Exceptions.DataException;
import Exceptions.InvalidGenotypeException;
import Exceptions.NotEnoughGenotypesException;
import Exceptions.WrongNumberOfSNPsException;
import Files.PlinkNumeric;
import Files.VCF.FormatDefinition;
import Files.VCF.Position;
import Files.VCF.VCF;
import Methods.Knni;
import Methods.KnniLD;
import Methods.Mode;
import Utils.LD;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Main executable class
 * @author Daniel Money
 */
public class LinkImpute
{
    /**
     * Main executable function
     * @param args Command line arguments - these are documented in the manual
     */

    /*
     * Quick summary of command line options
     * -p   Plink
     * -v   VCF
     * -a   Array
     *
     * -l   LD-knni
     * -k   knni
     * -m   Mode
     *
     * --neighbours=number
     * --snps=number
     */
    
    //No need for a public constructor!
    private LinkImpute()
    {
        
    }

    public static void main(String[] args)
    {
        Options options = new Options();
        
        OptionGroup fileFormat = new OptionGroup();
        fileFormat.addOption(Option.builder("p").desc("Use plink raw file format (default)").build());
        fileFormat.addOption(Option.builder("v").desc("Use VCF file format (experimental)").build());
        fileFormat.addOption(Option.builder("a").desc("Use array file format").build());
        options.addOptionGroup(fileFormat);
        
        OptionGroup method = new OptionGroup();
        method.addOption(Option.builder("m").desc("Use mode imputation").build());
        method.addOption(Option.builder("k").desc("Use kNN imputation").build());
        method.addOption(Option.builder("l").desc("Use LD-kNN imputation (default)").build());
        options.addOptionGroup(method);
        
        options.addOption(Option.builder().longOpt("neighbours").hasArg().desc("Number of neighbours to use in kNNi methods").build());
        options.addOption(Option.builder().longOpt("snps").hasArg().desc("The number of SNPs used to determine nearest neighbours in LD-kNNi").build());
        
        options.addOption(Option.builder().longOpt("help").desc("Display this help").build());
        
        CommandLineParser parser = new DefaultParser();
        
        try
        {
            boolean help = false;
            CommandLine commands = parser.parse(options, args);
            
            if (commands.hasOption("help"))
            {
                help = true;
            }
            else
            {
                if (commands.getArgList().size() != 2)
                {
                    if (commands.getArgList().size() < 2)
                    {
                        System.out.println("Must provide both an input and output file.");
                    }
                    else
                    {
                        System.out.println("Too many files specified. Usage is explained below.");
                    }
                    help = true;
                }
                
                if (commands.hasOption("m"))
                {                          
                    if (commands.hasOption("neighbours") || commands.hasOption("snps"))
                    {
                        System.out.println("Cannot use neighbours or snps options with Mode imputation");
                        help = true;
                    }
                }
                if (commands.hasOption("k"))
                {
                    if (commands.hasOption("snps"))
                    {
                        System.out.println("Cannot use snps options with kNN imputation");
                        help = true;
                    }
                    if (commands.hasOption("neighbours"))
                    {
                        try
                        {
                            int k = Integer.parseInt(commands.getOptionValue("neighbours"));
                            if (k <= 0)
                            {
                                System.out.println("Number of neighbours must be a positive integer");
                                help = true;
                            }
                        }
                        catch (NumberFormatException ex)
                        {
                            System.out.println("Number of neighbours must be a positive integer");
                            help = true;
                        }
                    }
                }
                
                if (!commands.hasOption("l") && !commands.hasOption("m"))
                {
                    if (commands.hasOption("neighbours"))
                    {
                        try
                        {
                            int k = Integer.parseInt(commands.getOptionValue("neighbours"));
                            if (k <= 0)
                            {
                                System.out.println("Number of neighbours must be a positive integer");
                                help = true;
                            }
                        }
                        catch (NumberFormatException ex)
                        {
                            System.out.println("Number of snps must be a positive integer");
                            help = true;
                        }
                    }
                    if (commands.hasOption("snps"))
                    {
                        try
                        {
                            int l = Integer.parseInt(commands.getOptionValue("snps"));
                            if (l <= 0)
                            {
                                System.out.println("Number of neighbours must be a positive integer");
                                help = true;
                            }
                        }
                        catch (NumberFormatException ex)
                        {
                            System.out.println("Number of neighbours must be a positive integer");
                            help = true;
                        }
                    }
                }
            }
            
            if (help)
            {
                System.out.println();
                help(options);
            }
            else
            {
                run(commands);
            }
        }
        catch (ParseException ex)
        {
            System.out.println("Invalid options selected.");
            System.out.println();
            help(options);
        }
        catch (DataException ex)
        {
            System.out.println("There is a problem with the input file.");
            System.out.println(ex.getMessage());
        }
        catch (IOException ex)
        {
            System.out.println("Problem reading / writing a file:");
            System.out.println(ex.getMessage());
        }
        catch (NotEnoughGenotypesException ex)
        {
            System.out.println(ex.getMessage());
        }
    }
    
    private static void help(Options options)
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setLongOptSeparator("=");
        String[] order = {"p", "a", "v", "l", "k", "m", "neighbours","snps","help"};
        formatter.setOptionComparator(new OptionOrder(order));
        formatter.printHelp("Impute [-p | -a | -v] [-l | -k | -m]\n" +
        "       [--neighbours=<arg>]  [--snps=<arg>] INFILE OUTFILE", 
                "\nImputes any missing values in the input file\n\n", options,
                "\nOutput file will be in the same format as the input and will be indentical "
                        + "except for missing values being replaced by imputed values.");
    }
    
    private static void run(CommandLine commands) throws IOException, DataException, NotEnoughGenotypesException
    {
        char fileFormat = 'p';
        if (commands.hasOption("v"))
        {
            fileFormat = 'v';
        }
        if (commands.hasOption("a"))
        {
            fileFormat = 'a';
        }
        
        char method = 'l';
        if (commands.hasOption("k"))
        {
            method = 'k';
        }
        if (commands.hasOption("m"))
        {
            method = 'm';
        }
        
        String in = commands.getArgList().get(0);
        String out = commands.getArgList().get(1);
        
        int k = Integer.parseInt(commands.getOptionValue("neighbours","5"));
        int l = Integer.parseInt(commands.getOptionValue("snps","20"));
        
        byte[][] original;
        
        PlinkNumeric pn = null;
        VCF vcf = null;
        
        switch (fileFormat)
        {
            case 'v':
                Map<String,Byte> map = new HashMap<>();
                map.put("0/0", (byte) 0);
                map.put("0/1", (byte) 1);
                map.put("1/1", (byte) 2);
                map.put("./.", (byte) -1);
                
                vcf = new VCF(new File(in));
                FormatDefinition gtF = vcf.getMeta().getFormatDefintion("GT");
                original = vcf.getData().asArray(gtF, map);                
                break;
            case 'a':
                original = readArray(new File(in));
                break;
            case 'p':
            default:
                pn = new PlinkNumeric(new File(in));
                original = pn.asArray();
                break;
        }
        
        System.out.println("Starting imputation on " + original.length + " samples and " +
            original[0].length + " SNPs...");
        byte[][] imputed;
        switch (method)
        {
            case 'm':
                Mode mode = new Mode();
                imputed = mode.compute(original);
                break;
            case 'k':
                Knni knni = new Knni(k);
                imputed = knni.compute(original);
                break;
            case 'l':
            default:
                double[][] ld = LD.calculate(transpose(original));
                KnniLD knnild = new KnniLD(ld,k,l);
                imputed = knnild.compute(original);
                break;
        }
        System.out.println("Finished imputation.");
        
        switch (fileFormat)
        {
            case 'v':
                writeVCFResult(new File (out), imputed, vcf);
                break;
            case 'a':
                writeArrayResult(new File(out), imputed);
                break;
            case 'p':
            default:
                writePlinkResult(new File(out), imputed, pn);
                break;
        }
    }
    
    private static byte[][] transpose(byte[][] o)
    {
        byte[][] n = new byte[o[0].length][o.length];
        for (int i = 0; i < o.length; i++)
        {
            for (int j = 0; j < o[i].length; j++)
            {
                n[j][i] = o[i][j];
            }
        }
        return n;
    }
    
    private static byte[][] readArray(File f) throws IOException, DataException
    {
        BufferedReader in = new BufferedReader(new FileReader(f));
        boolean first = true;
        int numSNPs = 0;
        int numsamp = 0;
        
        String line;
        List<byte[]> samples = new ArrayList<>();
        while ((line = in.readLine()) != null)
        {
            numsamp ++;
            String[] parts = line.split("\\s+");
            if (first)
            {
                numSNPs = parts.length;
            }
            if (parts.length != numSNPs)
            {
                throw new WrongNumberOfSNPsException(numsamp);
            }
            
            byte[] b = new byte[parts.length];
            for (int i = 0; i < parts.length; i++)
            {
                b[i] = getValue(parts[i]);
            }
            samples.add(b);
        }
        
        byte[][] ret = new byte[samples.size()][];
        return samples.toArray(ret);
    }
    
    private static byte getValue(String s) throws InvalidGenotypeException
    {
        try
        {
            Byte b = Byte.parseByte(s);
            if ((b < -1) || (b > 2))
            {
                throw new InvalidGenotypeException(s);
            }
            return b;
        }
        catch (NumberFormatException e)
        {
            throw new InvalidGenotypeException(s);
        }
    }
    
    private static void writeArrayResult(File f, byte[][] result) throws IOException
    {
        PrintStream out = new PrintStream(new FileOutputStream(f));
        
        for (byte[] r: result)
        {
            boolean first = true;
            for (byte b: r)
            {
                if (!first)
                {
                    out.print("\t");
                }
                out.print(b);
                first = false;
            }
            out.println();
        }
        
        out.close();
    }
    
    private static void writeVCFResult(File f, byte[][] result, VCF vcf) throws IOException
    {
        byte[][] resultT = transpose(result);
        List<Position> snps = vcf.getData().getPositions();
        FormatDefinition gtF = vcf.getMeta().getFormatDefintion("GT");
        
        int c = 0;
        for (Position p: snps)
        {
            byte[] r = resultT[c];
            String[] rs = new String[r.length];
            for (int i = 0; i < r.length; i++)
            {
                switch (r[i])
                {
                    case 0:
                        rs[i] = "0/0";
                        break;
                    case 1:
                        rs[i] = "0/1";
                        break;
                    case 2:
                        rs[i] = "1/1";
                        break;
                    default:
                        rs[i] = "./.";
                        break;
                }
            }
            vcf.getData().changeFormat(gtF, p, rs);
        }
     
        vcf.writeFile(f);
    }
    
    private static void writePlinkResult(File f, byte[][] result, PlinkNumeric orig) throws IOException
    {
        PrintStream out = new PrintStream(new FileOutputStream(f));
        
        String[] mh = orig.getMetaHead();
        out.print(mh[0]);
        for (int i = 1; i < mh.length; i++)
        {
            out.print(" ");
            out.print(mh[i]);
        }
        for (String s: orig.getSNPs())
        {
            out.print(" ");
            out.print(s);
        }
        out.println();
        
        for (int i = 0; i < result.length; i++)
        {
            String sample = orig.getSamples().get(i);
            String[] m = orig.getMeta(sample);
            out.print(m[0]);
            for (int j = 1; j < m.length; j++)
            {
                out.print(" ");
                out.print(m[j]);
            }
            byte[] r = result[i];
            for (int j = 0; j < r.length; j++)
            {
                out.print(" ");
                out.print(r[j]);
            }
            out.println();
        }
        out.close();
    }
    
    private static class OptionOrder implements Comparator<Option>
    {
        public OptionOrder(String[] order)
        {
            this(Arrays.asList(order));
        }
        
        public OptionOrder(List<String> order)
        {
            this.order = order;
        }
        
        public int compare(Option o1, Option o2)
        {
            String s1;
            if (o1.getOpt() == null)
            {
                s1 = o1.getLongOpt();
            }
            else
            {
                s1 = o1.getOpt();
            }
            
            String s2;
            if (o2.getOpt() == null)
            {
                s2 = o2.getLongOpt();
            }
            else
            {
                s2 = o2.getOpt();
            }
            return Integer.compare(order.indexOf(s1), order.indexOf(s2));
        }
        
        private List<String> order;
    }
}
