import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class SeasonList {
    private final List<String> colNames = new ArrayList<String>();
    private final List<ArrayList<Double>> years = new ArrayList<ArrayList<Double>>();
    public int firstSeason;

    private SeasonList(int fS) {
        firstSeason = fS;
    }

    public List<Double> getYear(int year) {
        int position = firstSeason - year;
        if (position < 0 || position >= years.size()) {
            return new ArrayList<Double>();
        }
        return years.get(position);
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
        SeasonList returnee = new SeasonList(Integer.parseInt(currVals[0]));
        returnee.colNames.addAll(Arrays.asList(colNames));
        ArrayList<Double> convertee = new ArrayList<Double>();
        for (int i = 1; i < currVals.length; i++) {
            convertee.add(Double.parseDouble(currVals[i]));
        }
        returnee.years.add(convertee);
        while ((line = br.readLine()) != null) {
            currVals = line.split(Scraper.CSV_SPLIT_BY);
            for (int i = 1; i < currVals.length; i++) {
                convertee.add(Double.parseDouble(currVals[i]));
            }
            returnee.years.add(convertee);
        }
        return returnee;
    }

}
