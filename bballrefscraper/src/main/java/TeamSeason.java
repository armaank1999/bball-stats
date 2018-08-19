import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Arrays;

public class TeamSeason {
    private static final String CSV_SPLIT_BY = ",";
    private static final String NEW_LINE = "\n";

    private Map<String, List<Double>> playerSeasons;
    private List<String> colNames;
    public int year;
    public String team;

    public TeamSeason(int y, String t) {
        year = y;
        team = t;
        playerSeasons = new LinkedHashMap<String, List<Double>>();
        colNames = new ArrayList<String>();
    }

    public void addPlayers(String[] newColNames, String[] names, double[][] colVals) {
        colNames.addAll(Arrays.asList(newColNames));
        for (int i = 0; i < names.length; i++) {
            List<Double> row = findOrCreateRow(names[i]);
            double[] basic = colVals[i];
            Double[] fancy = new Double[basic.length];
            for (int j = 0; j < basic.length; j++) {
                fancy[j] = basic[j];
            }
            List<Double> vals = Arrays.asList(fancy);
            row.addAll(vals);
        }
    }

    public void printAllInfo() {
        System.out.println("Parsed from " + url());
        System.out.printf("%-25s","Name");
        for (String colName : colNames) {
            System.out.printf("%-8s", colName);
        }
        System.out.println();
        for (String key : playerSeasons.keySet()) {
            printRow(key);
        }
    }

    public String url() {
        return Scraper.baseTeamUrl + team + "/" + year + ".html";
    }

    private List<Double> findOrCreateRow(String name) {
        if (!playerSeasons.containsKey(name)) {
            playerSeasons.put(name, new ArrayList<Double>());
        }
        return playerSeasons.get(name);
    }

    private void printRow(String name) {
        System.out.printf("%-25s", name);
        List<Double> row = playerSeasons.get(name);
        for (Double val : row) {
            System.out.printf("%-8s", val);
        }
        System.out.println();
    }

    private String rowCSV(String name) {
        StringBuilder line = new StringBuilder();
        line.append(name);
        line.append(CSV_SPLIT_BY);
        List<Double> row = playerSeasons.get(name);
        for (Double val : row) {
            line.append(val);
            line.append(CSV_SPLIT_BY);
        }
        line.append(NEW_LINE);
        return line.toString();
    }

    public void saveFile() throws Exception {
        File f = new File(team + year + ".csv");
        FileWriter output = new FileWriter(f);
        StringBuilder fileValue = new StringBuilder("Name,");
        for (String col : colNames) {
            fileValue.append(col);
            fileValue.append(CSV_SPLIT_BY);
        }
        fileValue.append(NEW_LINE);
        for (String name : playerSeasons.keySet()) {
            fileValue.append(rowCSV(name));
        }
        output.append(fileValue);
        output.flush();
        output.close();
    }

    private void deleteCol(String colName) {
        int index = colNames.lastIndexOf(colName);
        if (index != -1) {
            colNames.remove(index);
            for (List season : playerSeasons.values()) {
                season.remove(index);
            }
        }
    }

    public void deleteCols(String[] cols) {
        for (String name : cols) {
            deleteCol(name);
        }
    }

}
