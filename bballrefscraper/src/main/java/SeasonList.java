import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class SeasonList {
    private final List<String> colNames = new ArrayList<>();
    private final List<ArrayList<Double>> years = new ArrayList<>();
    private int firstSeason;

    private SeasonList(double fS) {
        firstSeason = (int) fS;
    }

    // Reads from the local csv file, which is from https://www.basketball-reference.com/leagues/NBA_stats.html but has many columns - TS%, etc - added.
    // The local excel file has that stuff and more, but this is only objective stuff - there I have graphs and subjective (but supported) analysis
    public static SeasonList readSeasonList(String fileName) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        // Read first lines outside of loop to get firstSeason and colNames
        String[] colNames = br.readLine().split(",");
        String line = br.readLine();
        String[] currVals = line.split(",");
        SeasonList returnee = new SeasonList(Double.parseDouble(currVals[0]));
        ArrayList<Double> convertee = new ArrayList<>();
        returnee.colNames.addAll(Arrays.asList(colNames));
        for (String val : currVals)
            convertee.add(Double.parseDouble(val));
        returnee.years.add(convertee);
        while ((line = br.readLine()) != null) {
            currVals = line.split(",");
            convertee = new ArrayList<>();
            for (String val : currVals)
                convertee.add(Double.parseDouble(val));
            returnee.years.add(convertee);
        }
        return returnee;
    }

    private static String rowCSV(List<Double> year) {
        StringBuilder sb = new StringBuilder();
        for (Double stat : year)
            sb.append(stat).append(",");
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public List<Double> getYear(int year) {
        int position = firstSeason - year;
        if (position < 0 || position >= years.size())
            return new ArrayList<>();
        return years.get(position);
    }

    public int[] getCols(String[] names) {
        int[] returnee = new int[names.length];
        for (int i = 0; i < names.length; i++)
            returnee[i] = colNames.indexOf(names[i]);
        return returnee;
    }

    public void printAllInfo() {
        for (String colName : colNames)
            System.out.printf("%-9s", colName);
        for (ArrayList<Double> year : years) {
            System.out.println();
            for (Double stat : year)
                System.out.printf("%-9s", stat);
        }
    }

    public void saveFile(String name) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", colNames)).append("\n");
        sb.append(String.join("\n", rowCSVs()));
        FileWriter output = new FileWriter(name + ".csv");
        output.append(sb).flush();
        output.close();
    }

    private String[] rowCSVs() {
        String[] csvs = new String[years.size()];
        int i = 0;
        for (ArrayList<Double> year : years)
            csvs[i++] = rowCSV(year);
        return csvs;
    }

}
