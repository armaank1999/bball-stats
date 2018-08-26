import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class SeasonList {
    private final List<String> colNames = new ArrayList<>();
    private final List<ArrayList<Double>> years = new ArrayList<>();
    public int firstSeason;

    private SeasonList(int fS) {
        firstSeason = fS;
    }

    public static SeasonList seasonFromFile(String fileName) throws Exception {
        String[] currVals;
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String[] colNames = br.readLine().split(Scraper.CSV_SPLIT_BY);
        // Read first line outside of loop to get firstSeason
        String line = br.readLine();
        if (line == null) {
            throw new Exception("File not formatted properly");
        }
        currVals = line.split(Scraper.CSV_SPLIT_BY);
        SeasonList returnee = new SeasonList((int) Double.parseDouble(currVals[0]));
        returnee.colNames.addAll(Arrays.asList(colNames));
        ArrayList<Double> convertee = new ArrayList<>();
        for (String val : currVals) {
            convertee.add(Double.parseDouble(val));
        }
        returnee.years.add(convertee);
        while ((line = br.readLine()) != null) {
            currVals = line.split(Scraper.CSV_SPLIT_BY);
            convertee = new ArrayList<>();
            for (String val : currVals) {
                convertee.add(Double.parseDouble(val));
            }
            returnee.years.add(convertee);
        }
        return returnee;
    }

    public List<Double> getYear(int year) {
        int position = firstSeason - year;
        if (position < 0 || position >= years.size()) {
            return new ArrayList<>();
        }
        return years.get(position);
    }

    public void printAllInfo() {
        for (String colName : colNames) {
            System.out.printf("%-9s", colName);
        }
        for (ArrayList<Double> year : years) {
            System.out.println();
            for (Double stat : year) {
                System.out.printf("%-9s", stat);
            }
        }
    }

    public void saveFile(String name) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(Scraper.CSV_SPLIT_BY, colNames));
        sb.append(String.join(Scraper.NEW_LINE, rowCSVs()));
        File f = new File(name + ".csv");
        FileWriter output = new FileWriter(f);
        output.append(sb);
        output.flush();
        output.close();
    }

    private String[] rowCSVs() {
        String[] csvs = new String[years.size()];
        int i = 0;
        for (ArrayList<Double> year : years) {
            csvs[i] = rowCSV(year);
            i += 1;
        }
        return csvs;
    }

    private static String rowCSV(List<Double> year) {
        StringBuilder sb = new StringBuilder();
        for (Double stat : year) {
            sb.append(stat);
            sb.append(Scraper.CSV_SPLIT_BY);
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

}
