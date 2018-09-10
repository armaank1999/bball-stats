import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Arrays;

public class TeamSeason {
    // Scaling constants for new stats, which should be addable to WS/48, so minAdjCoeff is in the right order of magnitude.
    // Issue for onOffCoeff - some teams have super drastic differences (09 Cavs, 16 Warriors) while others have players
    // close to each other (18 celtics, 04 Pistons). Divide by sqrt(stddev) or smth to partially normalize the gap?
    private final static double minAdjCoeff = 0.02;
    private final static double onOffCoeff = 0.05;
    private final static double rebCoeff = 0.002;

    public final String team;
    public final int year;
    private final List<String> playerColNames = new ArrayList<>();
    private final Map<String, ArrayList<Double>> playerSeasons = new LinkedHashMap<>();
    private final List<String> teamColNames = new ArrayList<>();
    private final List<Double> teamAttributes = new ArrayList<>();

    public TeamSeason(String t, int y) {
        year = y;
        team = t;
    }

    public String url() {
        return Scraper.baseTeamUrl + team + "/" + year + ".html";
    }

    public void printAllInfo() {
        System.out.printf("Parsed from %s\n", url());
        System.out.println("Team Stats: ");
        for (String colName : teamColNames)
            System.out.printf("%-9s", colName);
        System.out.println();
        for (Double val : teamAttributes)
            System.out.printf("%-9s", val);
        System.out.println();
        System.out.print("Name                       ");
        for (String colName : playerColNames)
            System.out.printf("%-9s", colName);
        for (String name : playerSeasons.keySet()) {
            System.out.printf("\n%-27s", name);
            for (Double val : playerSeasons.get(name))
                System.out.printf("%-9s", val);
        }
    }

    private String rowCSV(String name) {
        StringBuilder line = new StringBuilder(name);
        List<Double> row = playerSeasons.get(name);
        for (Double val : row)
            line.append(",").append(val);
        return line.toString();
    }

    private String[] rowCSVs() {
        String[] csvs = new String[playerSeasons.size()];
        int i = 0;
        for (String name : playerSeasons.keySet())
            csvs[i++] = rowCSV(name);
        return csvs;
    }

    public void saveFile() throws Exception {
        FileWriter output = new FileWriter(team + year + ".csv");
        StringBuilder fileValue = new StringBuilder("Name,");
        fileValue.append(String.join(",", playerColNames)).append("\n");
        fileValue.append(String.join("\n", rowCSVs()));
        output.append(fileValue).flush();
        output.close();
    }

    public void addPlayers(String[] newColNames, String[] names, double[][] colVals) {
        playerColNames.addAll(Arrays.asList(newColNames));
        for (int i = 0; i < names.length; i++) {
            List<Double> row = findOrCreateRow(names[i]);
            for (double val : colVals[i])
                row.add(val);
        }
    }

    public void addTeamAttributes(String[] newColNames, double[] vals) {
        teamColNames.addAll(Arrays.asList(newColNames));
        for (double val : vals)
            teamAttributes.add(val);
    }

    public void renameTeamCols(String[] originals, String[] news) {
        for (int i = 0; i < originals.length; i++)
            teamColNames.set(teamColNames.lastIndexOf(originals[i]), news[i]);
    }

    private List<Double> findOrCreateRow(String name) {
        if (!playerSeasons.containsKey(name))
            playerSeasons.put(name, new ArrayList<>());
        return playerSeasons.get(name);
    }

    public void deleteCols(String[] cols) {
        for (String name : cols)
            deleteCol(name);
    }

    public void deleteTeamCols(String[] cols) {
        for (String name : cols)
            deleteTeamCol(name);
    }

    public void deleteBlankCols() {
        while (deleteCol(""));
    }

    private boolean deleteCol(String colName) {
        int index = playerColNames.lastIndexOf(colName);
        if (index != -1) {
            playerColNames.remove(index);
            for (List season : playerSeasons.values())
                season.remove(index);
            return true;
        }
        return false;
    }

    private void deleteTeamCol(String colName) {
        int index = teamColNames.lastIndexOf(colName);
        if (index != -1) {
            teamColNames.remove(index);
            teamAttributes.remove(index);
        }
    }

    // Takes a column and adjusts each value so that the weighted sum is 0, then round to 4 decimal places.
    public void normalize(int colPos) {
        double total = 0, minutes = 0;
        int weightI = playerColNames.indexOf("MP");
        for (List<Double> row : playerSeasons.values()) {
            minutes += row.get(weightI);
            total += row.get(colPos) * row.get(weightI);
        }
        double average = total / minutes;
        for (List<Double> row : playerSeasons.values())
            row.set(colPos, Math.floor(10000 * (row.get(colPos) - average)) / 10000);
    }

    // Adds column based on the other adjustment methods and add a new column for the sum of them.
    public void addAdjustments() {
        addReboundingAdjustment();
        addOnOffAdjustments();
//        TODO: uncomment this once on off adjustment normalization has been figured out
//        playerColNames.add("AdjWS/48");
//        int wsI = playerColNames.indexOf("WS/48"), rbI = playerColNames.indexOf("RBAdj"), ooI = playerColNames.indexOf("OoAdj"), minI = playerColNames.indexOf("MinAdj");
//        for (List<Double> row : playerSeasons.values())
//            row.add(row.get(wsI) + row.get(rbI) + row.get(ooI) + row.get(minI));
    }

    // Subtract a multiple of individual ORB/DRB% and add in the on-off effect that player has on
    // the team rebounding wise to gauge actual impact/punish stat-padding, then normalize the column.
    // Also pretty universally hurts centers, but I feel that advanced stats in general overhype them
    // Javale, David West, Zaza WS/48 are way higher than Klay's, so keeping this feature
    private void addReboundingAdjustment() {
        int percentI = playerColNames.indexOf("%MP"), gamesI = playerColNames.indexOf("G"), orbI = playerColNames.indexOf("ORB%"),
                drbI = playerColNames.indexOf("DRB%"), netORBI = playerColNames.indexOf("OoORB%"), netDRBI = playerColNames.indexOf("OoDRB%");
        for (List<Double> row : playerSeasons.values()) {
            double weight = rebCoeff * Math.sqrt(row.get(gamesI)) * row.get(percentI) * (100 - row.get(percentI)) / 22500;
            // Subtract a multiple of ORB% and DRB%. Number should be somewhere between 0.5 and 1.
            double value = (row.get(netORBI) - 0.75 * row.get(orbI)) + (row.get(netDRBI) - 0.75 * row.get(drbI));
            row.add(value * weight);
        }
        normalize(playerColNames.size());
        playerColNames.add("RBAdj");
    }

    // Add/subtract points based on how much better the team/opponent's offensive ratings are. Weigh defensive impact more
    // as that is less captured by the other baseline advanced stats - number should be between 1 and 2.
    // Give points based on total games played minutes per game as clearly players who play more MPG are better
    // but WS/48 ignores that. Weight on off change based on sqrt games and %MP * (1 - %MP) - stddev for binomial.
    private void addOnOffAdjustments() {
        int minutesI = playerColNames.indexOf("MP"), percentI = playerColNames.indexOf("%MP"), gamesI = playerColNames.indexOf("G"),
                offOOI = playerColNames.indexOf("OoORtg"), defOOI = playerColNames.indexOf("OoDRtg");
        for (List<Double> row : playerSeasons.values()) {
            double GP = row.get(gamesI), MP = row.get(minutesI);
            double weight = onOffCoeff * Math.sqrt(GP) * row.get(percentI) * (100 - row.get(percentI)) / 22500;
            row.add((row.get(offOOI) - 1.5 * row.get(defOOI)) * weight);
            row.add(minAdjCoeff * (0.5 * Math.log(GP) + Math.log(Math.max(MP / GP - 5, 1)) + Math.sqrt((30 + MP) / (GP + 2))));
        }
        normalize(playerColNames.size());
        playerColNames.add("OoAdj");
        normalize(playerColNames.size());
        playerColNames.add("MinAdj");
    }

    public void addRelativeInfo(SeasonList averages) {

    }

//    Old potential formula from past project
//    // Add Points based on the player's usage rate and scoring efficiency .08 term gives extra points for having extra usage
//    // to not overglorify low usage players; netEff term credits players for being efficient. WS formula uses .92 so I use this.
//    // Then add points for providing floor spacing. The /20 is arbitrary.
//    // One could theoretically add a FTr term but there are upsides (stop transition scoring, foul trouble) and downsides
//    // (less offensive boards) as well as momentum.
//    private static double SPA(Season pS, Season yA) {
//        double netEff = pS.getElem("TS%") - yA.getElem("TS%");
//        double usage = pS.getElem("USG%");
//        double net3PR = pS.getElem("3PAr") - yA.getElem("3PAr");
//        return Math.floor((usage * (netEff + net3PR / 20.0) + .08 * (usage - 20.0)) * 1000) / 1000;
//    }

}
