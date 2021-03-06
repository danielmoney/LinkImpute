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

import Mask.Mask;
import Exceptions.DataException;
import Exceptions.InvalidGenotypeException;
import Exceptions.NotEnoughGenotypesException;
import Exceptions.WrongNumberOfSNPsException;
import Files.PlinkNumeric;
import Files.VCF;
import Methods.Knni;
import Methods.KnniLD;
import Methods.Mode;
import Correlation.Correlation;
import Correlation.Pearson;
import Files.PlinkPed;
import Files.VCFData.FormatDefinition;
import Files.VCFMappers.ByteMapper;
import Files.VCFData.Position;
import Methods.KnniLDOpt;
import Methods.KnniOpt;
import Utils.Optimize;
import Utils.Optimize.OptimizeException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    //No need for a public constructor!
    private LinkImpute()
    {
        
    }

    /**
     * Main executable function
     * @param args Command line arguments - these are documented in the manual
     */
    public static void main(String[] args)
    {
        Options options = new Options();
        
        OptionGroup fileFormat = new OptionGroup();
        fileFormat.addOption(Option.builder("p").desc("Use plink raw file format (default)").build());
        fileFormat.addOption(Option.builder("q").desc("Use plink ped file format").build());
        fileFormat.addOption(Option.builder("v").desc("Use VCF file format (experimental)").build());
        fileFormat.addOption(Option.builder("a").desc("Use array file format").build());
        options.addOptionGroup(fileFormat);
        
        OptionGroup method = new OptionGroup();
        method.addOption(Option.builder().longOpt("mode").desc("Use mode imputation instead of LD-kNNi").build());
        method.addOption(Option.builder().longOpt("knni").desc("Use kNN imputation instead of LD-kNNi").build());
        options.addOptionGroup(method);
        
        options.addOption(Option.builder().longOpt("fixedk").hasArg().desc("Fix the value of k to the given value").build());
        options.addOption(Option.builder().longOpt("fixedl").hasArg().desc("Fix the value of l to the given value").build());
        
        options.addOption(Option.builder().longOpt("ldout").hasArg().desc("Output the snps most in LD with each snp to the given file").build());
        options.addOption(Option.builder().longOpt("ldnum").hasArg().desc("Output the given number of snps most in LD. Defaults to 65").build());
        options.addOption(Option.builder().longOpt("ldin").hasArg().desc("Read LD information from the given file rather than calculate it").build());
        options.addOption(Option.builder().longOpt("ldonly").desc("Do not perform the imputation.  Use to obtain just the LD information").build());
        
        options.addOption(Option.builder().longOpt("nummask").hasArg().desc("Number of genotypes to mask when optimizing.").build());
        
        options.addOption(Option.builder().longOpt("verbose").desc("Display detailed run information").build());
        
        options.addOption(Option.builder().longOpt("help").desc("Display this help").build());
        
        options.addOption(Option.builder().longOpt("version").desc("Display version information").build());
        
        CommandLineParser parser = new DefaultParser();
        
        try
        {
            boolean help = false;
            boolean version = false;
            CommandLine commands = parser.parse(options, args);
            
            if (commands.hasOption("help"))
            {
                help = true;
            }
            if (commands.hasOption("version"))
            {
                version = true;
            }
            if (!(version || help))
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
                
                if (commands.hasOption("knni") || commands.hasOption("mode"))
                {
                    if (commands.hasOption("ldnum") || commands.hasOption("ldin")
                            || commands.hasOption("ldout") || commands.hasOption("ldonly"))
                    {
                        System.out.println("LD options can only be used with LD-kNNi");
                        help = true;
                    }
                }                               
                else
                {
                    if (commands.hasOption("ldin"))
                    {
                        if (commands.hasOption("ldnum"))
                        {
                            System.out.println("ldnum and ldin options cannot be used together");
                            help = true;
                        }
                        else
                        {
                            File f = new File(commands.getOptionValue("ldin"));
                            if (!f.canRead())
                            {
                                System.out.println("Cannot read ldin file");
                                help = true;
                            }
                        }
                    }
                }
                
                if (commands.hasOption("fixedl") && 
                        (commands.hasOption("mode") || commands.hasOption("knni")))
                {
                    System.out.println("fixedl option cannot be used with mode or knni"
                            + "options");
                    help = true;
                }
                if (commands.hasOption("fixedk") && commands.hasOption("mode"))
                {
                    System.out.println("fixedk option cannot be used with mode "
                            + "option");
                }
                if (!(commands.hasOption("mode") || commands.hasOption("knni")) &&
                        (commands.hasOption("fixedk") ^ commands.hasOption("fixedl")))
                {
                        System.out.println("fixedk and fixedl options must be used"
                                + "together for LD-knni imputation");
                }
                
                help = badNumeric(commands,"ldnum") | help;
                help = badNumeric(commands,"fixedk") | help;
                help = badNumeric(commands,"fixedl") | help;
                help = badNumeric(commands,"nummask") | help;                
            }
            
            if (help)
            {
                System.out.println();
                help(options);
            }
            if (version)
            {
                System.out.println("Version 1.1.4 (26 July 2019)");
            }
            if (!(version || help))
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
        catch (DataException | OptimizeException ex)
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
    
    private static boolean badNumeric(CommandLine commands, String option)
    {
        boolean bad = false;
        if (commands.hasOption(option))
        {
            try
            {
                if (Integer.parseInt(commands.getOptionValue(option)) <= 0)
                {
                    bad = true;
                }
            }
            catch(NumberFormatException ex)
            {
                bad = true;
            }
            if (bad)
            {
                System.out.println("Arguement to " + option + " must be an integer"
                        + " greater than zero");
            }
        }
        return bad;
    }
    
    private static void help(Options options)
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setLongOptSeparator("=");
        String[] order = {"p", "q", "a", "v", "knni", "mode", "verbose","fixedk", "fixedl",
            "ldin","ldout","ldnum","ldonly","nummask","version","help"};
        formatter.setOptionComparator(new OptionOrder(order));
        formatter.printHelp("LinkImpute [-p | -q | -a | -v] [--mode | --knni] \n" +
        "       [--fixedk=<arg>] [--fixedl=<arg>] \n" +
        "       [--ldout=<arg>] [--ldnum=<arg>] [--ldin=<arg>] [--ldonly]\n" +
        "       [--nummask=<arg]\n" +
        "       INFILE OUTFILE", 
                "\nImputes any missing values in the input file\n\n", options,
                "\nOutput file will be in the same format as the input and will be indentical "
                        + "except for missing values being replaced by imputed values.");
    }
    
    private static void run(CommandLine commands) throws IOException, DataException, NotEnoughGenotypesException, OptimizeException
    {
        long start = System.currentTimeMillis();
        FileFormat fileFormat = FileFormat.RAW;
        if (commands.hasOption("q"))
        {
            fileFormat = FileFormat.PED;
        }
        if (commands.hasOption("v"))
        {
            fileFormat = FileFormat.VCF;
        }
        if (commands.hasOption("a"))
        {
            fileFormat = FileFormat.ARRAY;
        }
        
        Method method = Method.LDKNNI;
        if (commands.hasOption("knni"))
        {
            method = Method.KNNI;
        }
        if (commands.hasOption("mode"))
        {
            method = Method.MODE;
        }
        
        boolean verbose = commands.hasOption("verbose");
        
        String in = commands.getArgList().get(0);
        String out = commands.getArgList().get(1);
        
        byte[][] original;
        
        PlinkNumeric pn = null;
        PlinkPed pp = null;
        VCF vcf = null;
        
        System.out.println("\nStarting to read in dataset...");
        long partstart = System.currentTimeMillis();
        switch (fileFormat)
        {
            case VCF:
                vcf = new VCF(new File(in));
                FormatDefinition gtF = vcf.getMeta().getFormatDefintion("GT");
                original = vcf.getData().asByteArray(gtF, new GenoToByte());                
                break;
            case ARRAY:
                original = readArray(new File(in));
                break;
            case PED:
                pp = new PlinkPed(new File(in));
                original = pp.asArray();
                break;
            case RAW:
            default:
                pn = new PlinkNumeric(new File(in));
                original = pn.asArray();
                break;
        }

        System.out.println("\tRead in data set of " + original.length + " samples and " +
            original[0].length + " SNPs.");
        if (verbose)
        {
            long time = (System.currentTimeMillis() - partstart) / 1000;
            System.out.println("Finished reading in data set (" + time + " seconds).");
        }
        else
        {
            System.out.println("Finished reading in data set.");
        }
        
        Correlation corr = new Pearson();
        Map<Integer,List<Integer>> ld = null;
        if (method == Method.LDKNNI)
        {
            if (!commands.hasOption("ldin"))
            {
                System.out.println("Starting calculating correlations...");
                partstart = System.currentTimeMillis();
                int number = Integer.parseInt(commands.getOptionValue("ldnum", "65"));
                ld = corr.topn(transpose(original), number);
                if (verbose)
                {
                    long time = (System.currentTimeMillis() - partstart) / 1000;
                    System.out.println("Finished calculating correlations (" + time + " seconds).");
                }
                else
                {
                    System.out.println("Finished calculating correlations.");
                }
            }
            else
            {
                ld = readLD(new File(commands.getOptionValue("ldin")));
            }
            if (commands.hasOption("ldout"))
            {
                writeLD(new File(commands.getOptionValue("ldout")),ld);
            }
        }
        
        if (!commands.hasOption("noimpute"))
        {
            int nummask = Integer.parseInt(commands.getOptionValue("nummask", "10000"));
            Mask mask = new Mask(original,nummask);
            
            byte[][] imputed;
            switch (method)
            {
                case MODE:
                    System.out.println("Starting calculating accuracy...");
                    partstart = System.currentTimeMillis();
                    Mode mode = new Mode();
                    double accuracy = mode.fastAccuracy(original, mask);
                    System.out.println("\tAccuracy:\t" + accuracy);
                    if (verbose)
                    {
                        long time = (System.currentTimeMillis() - partstart) / 1000;
                        System.out.println("Starting calculating accuracy (" + time + " seconds)." );
                    }
                    else
                    {
                        System.out.println("Starting calculating accuracy.");
                    }

                    System.out.println("Starting imputation...");
                    partstart = System.currentTimeMillis();
                    imputed = mode.compute(original);
                    if (verbose)
                    {
                        long time = (System.currentTimeMillis() - partstart) / 1000;
                        System.out.println("Finished imputation (" + time + " seconds).");
                    }
                    else
                    {
                        System.out.println("Finished imputation.");
                    }
                    break;
                case KNNI:
                    double[][] weight = Knni.weight(original);
                    int k;
                    if (commands.hasOption("fixedk"))
                    {
                        k = Integer.parseInt(commands.getOptionValue("fixedk"));
                        System.out.println("Estimating accuracy...");
                        partstart = System.currentTimeMillis();                  

                        Knni knni = new Knni(k);
                        System.out.println("\tAccuracy:\t" + knni.fastAccuracy(original, mask, weight));
                        
                        if (verbose)
                        {
                            long time = (System.currentTimeMillis() - partstart) / 1000;
                            System.out.println("Finished estimating accuracy (" + time + " seconds).");
                        }
                        else
                        {
                            System.out.println("Finished estimating accuracy.");
                        }
                    }
                    else
                    {                        
                        System.out.println("Starting optimizing parameters...");
                        partstart = System.currentTimeMillis();
                        KnniOpt knniopt = new KnniOpt(original,mask,weight,verbose);
                        int[] startmax = {9};
                        int[] absmax = {original.length};
                        Optimize ok = new Optimize(knniopt,startmax,absmax);
                        if (!verbose)
                        {
                            System.out.println();
                        }

                        System.out.println("\tBest k:\t" + ok.getBestParameter()[0]);
                        System.out.println("\tAccuracy:\t" + ok.getBestValue());
                        if (verbose)
                        {
                            long time = (System.currentTimeMillis() - partstart) / 1000;
                            System.out.println("Finished optimizing parameters (" + time + " seconds).");
                        }
                        else
                        {
                            System.out.println("Finished optimizing parameters.");
                        }
                        k = ok.getBestParameter()[0];
                    }
                    
                    System.out.println("Starting imputation...");
                    partstart = System.currentTimeMillis();
                    Knni knni = new Knni(k);
                    imputed = knni.compute(original,weight);
                    if (verbose)
                    {
                        long time = (System.currentTimeMillis() - partstart) / 1000;
                        System.out.println("Finished imputation (" + time + " seconds).");
                    }
                    else
                    {
                        System.out.println("Finished imputation.");
                    }
                    break;
                case LDKNNI:
                default:
                    int l;
                    if (commands.hasOption("fixedk"))
                    {
                        k = Integer.parseInt(commands.getOptionValue("fixedk"));
                        l = Integer.parseInt(commands.getOptionValue("fixedl"));
                        System.out.println("Estimating accuracy...");
                        partstart = System.currentTimeMillis();                  

                        KnniLD knnild = new KnniLD(ld,k,l);
                        System.out.println("\tAccuracy:\t" + knnild.fastAccuracy(original, mask));
                        
                        if (verbose)
                        {
                            long time = (System.currentTimeMillis() - partstart) / 1000;
                            System.out.println("Finished estimating accuracy (" + time + " seconds).");
                        }
                        else
                        {
                            System.out.println("Finished estimating accuracy.");
                        }
                    }
                    else
                    {
                        System.out.println("Starting optimizing parameters...");
                        partstart = System.currentTimeMillis();
                        KnniLDOpt knnildopt = new KnniLDOpt(original,mask,ld,verbose);
                        int[] startmaxld = {9,17};
                        int[] absmaxld = {original.length,ld.get(0).size()};
                        Optimize ol = new Optimize(knnildopt,startmaxld,absmaxld);
                        if (!verbose)
                        {
                            System.out.println();
                        }

                        System.out.println("\tBest k:\t\t" + ol.getBestParameter()[0]);
                        System.out.println("\tBest l:\t\t" + ol.getBestParameter()[1]);
                        System.out.println("\tAccuracy:\t" + ol.getBestValue());
                        if (verbose)
                        {
                            long time = (System.currentTimeMillis() - partstart) / 1000;
                            System.out.println("Finished optimizing parameters (" + time + " seconds).");
                        }
                        else
                        {
                            System.out.println("Finished optimizing parameters.");
                        }
                        k = ol.getBestParameter()[0];
                        l = ol.getBestParameter()[1];
                    }

                    System.out.println("Starting imputation...");
                    partstart = System.currentTimeMillis();
                    KnniLD knnild = new KnniLD(ld,k,l);
                    imputed = knnild.compute(original);
                    if (verbose)
                    {
                        long time = (System.currentTimeMillis() - partstart) / 1000;
                        System.out.println("Finished imputation (" + time + " seconds).");
                    }
                    else
                    {
                        System.out.println("Finished imputation.");
                    }
                    break;
            }

            System.out.println("Writing output...");
            partstart = System.currentTimeMillis();
            switch (fileFormat)
            {
                case VCF:
                    writeVCFResult(new File (out), imputed, vcf);
                    break;
                case ARRAY:
                    writeArrayResult(new File(out), imputed);
                    break;
                case PED:
                    writePedResult(new File(out), imputed, pp);
                    break;
                case RAW:
                default:
                    writeRawResult(new File(out), imputed, pn);
                    break;
            }
            if (verbose)
            {
                long time = (System.currentTimeMillis() - partstart) / 1000;
                System.out.println("Finished writing output (" + time + " seconds).");
            }
            else
            {
                System.out.println("Finished writing output.");
            }
        }
        else
        {
            System.out.println("Not performing imputation (--noimpute option given).");
        }
        if (verbose)
        {
            long time = (System.currentTimeMillis() - start) / 1000;
            System.out.println("Total run time: " + time + " seconds");
        }
        System.out.println();
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
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        
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
            c++;
        }
     
        vcf.writeFile(f);
    }
    
    private static void writePedResult(File f, byte[][] result, PlinkPed orig) throws IOException
    {
        PlinkPed newpp = orig.changeData(result);
        newpp.writeToFile(f);
    }
    
    private static void writeRawResult(File f, byte[][] result, PlinkNumeric orig) throws IOException
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        
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
    
    private static void writeLD(File f, Map<Integer,List<Integer>> data) throws IOException
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        
        for (Entry<Integer,List<Integer>> d: data.entrySet())
        {
            out.print(d.getKey());
            for (Integer i: d.getValue())
            {
                out.print("\t");
                out.print(i);
            }
            out.println();
        }
        out.close();
    }
    
    private static Map<Integer,List<Integer>> readLD(File f) throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(f));
        String line;
        Map<Integer,List<Integer>> data = new HashMap<>();
        while ((line = in.readLine()) != null)
        {
            String[] parts = line.split("\t");
            List<Integer> set = new ArrayList<>();
            for (int i = 1; i < parts.length; i++)
            {
                set.add(Integer.valueOf(parts[i]));
            }
            data.put(Integer.valueOf(parts[0]),set);
        }
        in.close();
        return data;
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
        
        @Override
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
    
    private static class GenoToByte implements ByteMapper
    {
        public GenoToByte()
        {
            map = new HashMap<>();
            map.put("0/0", (byte) 0);
            map.put("0/1", (byte) 1);
            map.put("1/0", (byte) 1);
            map.put("1/1", (byte) 2);
            map.put("./.", (byte) -1);
            map.put("0|0", (byte) 0);
            map.put("0|1", (byte) 1);
            map.put("1|0", (byte) 1);
            map.put("1|1", (byte) 2);
            map.put(".|.", (byte) -1);
        }
        
        @Override
        public byte map(String s)
        {
            return map.get(s);
        }
        
        private final HashMap<String,Byte> map;
    }
    
    private enum FileFormat
    {
        RAW,
        PED,
        VCF,
        ARRAY
    }
    
    private enum Method
    {
        MODE,
        KNNI,
        LDKNNI
    }
}
