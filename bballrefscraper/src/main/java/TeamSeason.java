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
    public final String team;
    public final int year;
    private Map<String, List<Double>> playerSeasons;
    private List<String> colNames;

    public TeamSeason(String t, int y) {
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
        System.out.printf("%-25s", "Name");
        for (String colName : colNames) {
            System.out.printf("%-9s", colName);
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
            System.out.printf("%-9s", val);
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

    private boolean deleteCol(String colName) {
        int index = colNames.lastIndexOf(colName);
        if (index != -1) {
            colNames.remove(index);
            for (List season : playerSeasons.values()) {
                season.remove(index);
            }
        }
        return index != -1;
    }

    public void deleteCols(String[] cols) {
        for (String name : cols) {
            deleteCol(name);
        }
    }

    public void deleteBlankCols() {
        while (deleteCol("")) {
        }
    }

    public void normalize(int colPos) {
        double total = 0.0;
        double totalMinutes = 0.0;
        int minutesPos = colNames.indexOf("MP");
        for (List<Double> row : playerSeasons.values()) {
            totalMinutes += row.get(minutesPos);
            total += row.get(colPos) * row.get(minutesPos);
        }
        double average = 5 * total / totalMinutes;
        for (List<Double> row : playerSeasons.values()) {
            row.set(colPos, row.get(colPos) - average);
        }
    }

    public void addAdjustments() {
        addReboundingAdjustment();
        addOnOffAdjustment();
    }

    private void addReboundingAdjustment() {
        int percentIndex = colNames.indexOf("%MP");
        int gamesIndex = colNames.indexOf("G");
        int orbIndex = colNames.indexOf("ORB%");
        int drbIndex = colNames.indexOf("DRB%");
        int netORBIndex = colNames.indexOf("+-TmORB%");
        int netDRBIndex = colNames.indexOf("+-TmDRB%");
        int adjIndex = colNames.size();
        colNames.add("RBAdj");
        for (List<Double> row : playerSeasons.values()) {
            double weight = row.get(percentIndex) * row.get(percentIndex) / 10000;
            double value = 0.0;
            row.set(adjIndex, value * weight);
        }

    }

    private void addOnOffAdjustment() {
        int percentIndex = colNames.indexOf("%MP");
        int gamesIndex = colNames.indexOf("G");
        int offenseOnOffIndex = colNames.indexOf("+-TmORtg");
        int defenseOnOffIndex = colNames.indexOf("+-OpORtg");
        int adjIndex = colNames.size();
        colNames.add("+-Adj");
        for (List<Double> row : playerSeasons.values()) {
            double weight = row.get(gamesIndex) * row.get(percentIndex) * row.get(percentIndex) / 820000;
            double value = 0.0;
            row.set(adjIndex, value * weight);
        }
    }

}
