import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Arrays;

public class TeamSeason {
    public final String team;
    public final int year;
    private final List<String> colNames = new ArrayList<String>();
    private final Map<String, List<Double>> playerSeasons = new LinkedHashMap<String, List<Double>>();

    public TeamSeason(String t, int y) {
        year = y;
        team = t;
    }

    public void addPlayers(String[] newColNames, String[] names, double[][] colVals) {
        colNames.addAll(Arrays.asList(newColNames));
        for (int i = 0; i < names.length; i++) {
            List<Double> row = findOrCreateRow(names[i]);
            double[] basic = colVals[i];
            for (double val : basic) {
                row.add(val);
            }
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
        line.append(Scraper.NEW_LINE);
        line.append(name);
        List<Double> row = playerSeasons.get(name);
        for (Double val : row) {
            line.append(Scraper.CSV_SPLIT_BY);
            line.append(val);
        }
        return line.toString();
    }

    public void saveFile() throws Exception {
        File f = new File(team + year + ".csv");
        FileWriter output = new FileWriter(f);
        StringBuilder fileValue = new StringBuilder("Name");
        for (String col : colNames) {
            fileValue.append(Scraper.CSV_SPLIT_BY);
            fileValue.append(col);
        }
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
            return true;
        }
        return false;
    }

    public void deleteCols(String[] cols) {
        for (String name : cols) {
            deleteCol(name);
        }
    }

    public boolean deleteBlankCols() {
        return deleteCol("") && deleteBlankCols();
    }

//    OR the original
//    public void deleteBlankCols() {
//        while (deleteCol("")) {
//        }
//    }
//    Not sure which is better practice/faster.

    // Takes a column and adjusts each player's value so that the net minute-weighted sum is 0.
    public void normalize(int colPos) {
        double total = 0.0;
        double totalMinutes = 0.0;
        int weightPos = colNames.indexOf("MP");
        for (List<Double> row : playerSeasons.values()) {
            totalMinutes += row.get(weightPos);
            total += row.get(colPos) * row.get(weightPos);
        }
        double average = 5 * total / totalMinutes;
        for (List<Double> row : playerSeasons.values()) {
            row.set(colPos, row.get(colPos) - average);
        }
    }

    // Adds column based on the other adjustment methods and add a new column for the sum of them.
    // TODO: round vals to 4 places after decimal point - (floor x * 10000)/10000
    public void addAdjustments() {
        addReboundingAdjustment();
        addOnOffAdjustment();
    }

    // Subtract a multiple of individual ORB/DRB% and add in a multiple of the effect that player has on
    // the team rebounding wise to gauge actual impact/punish stat-padding, then normalize the column.
    private void addReboundingAdjustment() {
        int percentIndex = colNames.indexOf("%MP");
        int gamesIndex = colNames.indexOf("G");
        int orbIndex = colNames.indexOf("ORB%");
        int drbIndex = colNames.indexOf("DRB%");
        int netORBIndex = colNames.indexOf("+-TmORB%");
        int netDRBIndex = colNames.indexOf("+-TmDRB%");
        for (List<Double> row : playerSeasons.values()) {
            double weight = row.get(percentIndex) * row.get(percentIndex) / 10000;
            double value = 0.0;
            row.add(value * weight);
        }
        normalize(colNames.size());
        colNames.add("RBAdj");
    }

    // Add/subtract points based on how much better the team/opponent's offensive ratings are.
    // Weigh defensive impact more as that is less captured by the other baseline advanced stats.
    // Give points based on minutes per game as clearly players who play more MPG are better
    // but WS/48 over corrects for that.
    private void addOnOffAdjustment() {
        int totalMinutesIndex = colNames.indexOf("MP");
        int percentIndex = colNames.indexOf("%MP");
        int gamesIndex = colNames.indexOf("G");
        int offenseOnOffIndex = colNames.indexOf("+-TmORtg");
        int defenseOnOffIndex = colNames.indexOf("+-OpORtg");
        int adjIndex = colNames.size();
        for (List<Double> row : playerSeasons.values()) {
            double gamesPlayed = row.get(gamesIndex);
            double minutesPlayed = row.get(totalMinutesIndex);
            // Just something random thrown around. Adjusts for total games played and minutes per game.
            double minuteAdjustment = ((Math.log(gamesPlayed) + 2.0 * Math.log(Math.max(minutesPlayed / gamesPlayed - 5, 1))
                    + 2.0 * Math.sqrt((30 + minutesPlayed) / (gamesPlayed + 2))) - 20) / 10;
            // Weight based on sqrt of games played and if they play exactly half the minutes, that means the data as as valid
            // as possible, so multiply % by 1 - %.
            double weight = Math.sqrt(row.get(gamesIndex)) * row.get(percentIndex) * (100 - row.get(percentIndex)) / 22500;
            double value = 0.0;
            row.set(adjIndex, value * weight + minuteAdjustment);
        }
        normalize(colNames.size());
        colNames.add("+-Adj");
    }

//    /* Add Points based on the player's usage rate and scoring efficiency .08 term gives extra points for having extra usage
//     to not overglorify low usage players; netEff term credits players for being efficient. WS formula uses .92 so I use this.
//     Then add points for providing floor spacing. The /20 is arbitrary.
//     One could theoretically add a FTr term but there are upsides (stop transition scoring, foul trouble) and downsides
//     (less offensive boards) as well as momentum. */
//    private static double SPA(Season pS, Season yA) {
//        double netEff = pS.getElem("TS%") - yA.getElem("TS%");
//        double usage = pS.getElem("USG%");
//        double net3PR = pS.getElem("3PAr") - yA.getElem("3PAr");
//        return Math.floor((usage * (netEff + net3PR / 20.0) + .08 * (usage - 20.0)) * 1000) / 1000;
//    }

}
