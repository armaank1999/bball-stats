import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Arrays;

public class TeamSeason {
    private Map<String, List<Double>> playerSeasons;
    private List<String> colNames;
    private int year;
    private String team;

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

}
