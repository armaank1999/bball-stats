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
    // The base URL for all pages
    private static final String baseUrl = "https://www.basketball-reference.com/teams/";

    private final String team;
    private final int year;
    private final List<String> playerCols = new ArrayList<>();
    private final Map<String, ArrayList<Double>> playerSeasons = new LinkedHashMap<>();
    private final List<String> teamCols = new ArrayList<>();
    private final List<Double> teamStats = new ArrayList<>();

    public TeamSeason(String t, int y) {
        year = y;
        team = t;
    }

    public String url(String ending) {
        return baseUrl + team + "/" + year + ending;
    }

    public void printAllInfo() {
        System.out.printf("Parsed from %s\n", url(".html"));
        System.out.println("Team Stats: ");
        for (String colName : teamCols)
            System.out.printf("%-9s", colName);
        System.out.println();
        for (Double val : teamStats)
            System.out.printf("%-9s", val);
        System.out.println();
        System.out.print("Name                       ");
        for (String colName : playerCols)
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
        FileWriter output = new FileWriter("teamOutput/" + team + year + ".csv");
        StringBuilder fileValue = new StringBuilder("Name,");
        fileValue.append(String.join(",", playerCols)).append("\n").append(String.join("\n", rowCSVs()));
        output.append(fileValue).flush();
        output.close();
        if (teamCols.size() > 0) {
            FileWriter teamOutput = new FileWriter("relativeOutput/" + team + year + "rel.csv");
            StringBuilder teamValue = new StringBuilder();
            teamValue.append(String.join(",", teamCols)).append("\n");
            for (double stat : teamStats)
                teamValue.append(stat).append(",");
            teamValue.setLength(teamValue.length() - 1);
            teamOutput.append(teamValue).flush();
            teamOutput.close();
        }
    }

    public void addPlayers(String[] newColNames, String[] names, double[][] colVals) {
        playerCols.addAll(Arrays.asList(newColNames));
        for (int i = 0; i < names.length; i++) {
            List<Double> row = findOrCreateRow(names[i]);
            for (double val : colVals[i])
                row.add(val);
        }
    }

    public void addTeamAttributes(String[] newColNames, double[] vals) {
        teamCols.addAll(Arrays.asList(newColNames));
        for (double val : vals)
            teamStats.add(val);
    }

    public void renameTeamCols(String[] originals, String[] news) {
        for (int i = 0; i < originals.length; i++)
            teamCols.set(teamCols.lastIndexOf(originals[i]), news[i]);
    }

    private List<Double> findOrCreateRow(String name) {
        if (!playerSeasons.containsKey(name))
            playerSeasons.put(name, new ArrayList<>());
        return playerSeasons.get(name);
    }

    private int[] getTeamCols(String[] names) {
        int[] returnee = new int[names.length];
        for (int i = 0; i < names.length; i++)
            returnee[i] = teamCols.indexOf(names[i]);
        return returnee;
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
        int index = playerCols.lastIndexOf(colName);
        if (index != -1) {
            playerCols.remove(index);
            for (List season : playerSeasons.values())
                season.remove(index);
            return true;
        }
        return false;
    }

    private void deleteTeamCol(String colName) {
        int index = teamCols.lastIndexOf(colName);
        if (index != -1) {
            teamCols.remove(index);
            teamStats.remove(index);
        }
    }

    // Takes a column and adjusts each value so that the weighted sum is 0, then round to 4 decimal places.
    private void normalize(int colPos) {
        double total = 0, minutes = 0;
        int weightI = playerCols.indexOf("MP");
        for (List<Double> row : playerSeasons.values()) {
            minutes += row.get(weightI);
            total += row.get(colPos) * row.get(weightI);
        }
        double average = total / minutes;
        for (List<Double> row : playerSeasons.values())
            row.set(colPos, Math.floor(10000 * (row.get(colPos) - average)) / 10000);
    }

    // Takes a column and divides each value by sqrt(stddev) to semi adjust gaps that are too large.
    private void semiNormalize(int colPos) {
        int weightPos = playerCols.indexOf("%MP");
        double stdDev = 0;
        for (ArrayList<Double> player : playerSeasons.values())
            stdDev += player.get(colPos) * player.get(colPos) * player.get(weightPos);
        for (ArrayList<Double> player : playerSeasons.values())
            player.set(colPos, Math.floor(10000 * player.get(colPos) / Math.sqrt(stdDev)) / 10000);
    }

    // Adds column based on the other adjustment methods and add a new column for the sum of them.
    public void addAdjustments() {
        addReboundingAdjustment();
        addOnOffAdjustments();
        playerCols.add("AdjWS/48");
        int wsI = playerCols.indexOf("WS/48"), rbI = playerCols.indexOf("RBAdj"),
            ooI = playerCols.indexOf("OoAdj"), minI = playerCols.indexOf("MinAdj");
        for (List<Double> row : playerSeasons.values())
            row.add((double) Math.round((row.get(wsI) + row.get(rbI) + row.get(ooI) + row.get(minI)) * 10000) / 10000);
    }

    // Subtract a multiple of individual ORB/DRB% and add in the on-off effect that player has on
    // the team rebounding wise to gauge actual impact/punish stat-padding, then normalize the column.
    // Also pretty universally hurts centers, but I feel that advanced stats in general overhype them
    // Javale, David West, Zaza WS/48 are way higher than Klay's, so keeping this feature
    private void addReboundingAdjustment() {
        int percentI = playerCols.indexOf("%MP"), gamesI = playerCols.indexOf("G"), orbI = playerCols.indexOf("ORB%"),
            drbI = playerCols.indexOf("DRB%"), netORBI = playerCols.indexOf("OoORB%"), netDRBI = playerCols.indexOf("OoDRB%");
        // Subtract a multiple of ORB% and DRB%. Number should be somewhere between 0.5 and 1. Then add in net effect.
        for (List<Double> row : playerSeasons.values())
            row.add((row.get(netORBI) + row.get(netDRBI) - 0.75 * row.get(orbI) - 0.75 * row.get(drbI)) *
                (rebCoeff * Math.sqrt(row.get(gamesI)) * row.get(percentI) * (100 - row.get(percentI)) / 22500));
        normalize(playerCols.size());
        playerCols.add("RBAdj");
    }

    // Add/subtract points based on how much better the team/opponent's offensive ratings are. Weigh defensive impact more
    // as that is less captured by the other baseline advanced stats - number should be between 1 and 2.
    // Give points based on total games played minutes per game as clearly players who play more MPG are better
    // but WS/48 ignores that. Weight on off change based on sqrt games and %MP * (1 - %MP) - stddev for binomial.
    private void addOnOffAdjustments() {
        int minutesI = playerCols.indexOf("MP"), percentI = playerCols.indexOf("%MP"), gamesI = playerCols.indexOf("G"),
            offOOI = playerCols.indexOf("OoORtg"), defOOI = playerCols.indexOf("OoDRtg");
        for (List<Double> row : playerSeasons.values()) {
            double GP = row.get(gamesI), MP = row.get(minutesI);
            row.add((row.get(offOOI) - 1.5 * row.get(defOOI)) *
                (onOffCoeff * Math.sqrt(GP) * row.get(percentI) * (100 - row.get(percentI)) / 22500));
            row.add(minAdjCoeff * (0.5 * Math.log(GP) + Math.log(Math.max(MP / GP - 5, 1)) + Math.sqrt((30 + MP) / (GP + 2))));
        }
        normalize(playerCols.size());
        semiNormalize(playerCols.size());
        playerCols.add("OoAdj");
        normalize(playerCols.size());
        playerCols.add("MinAdj");
    }

    public void addRelativeInfo(SeasonList averages) {
        List<Double> comparee = averages.getYear(year);
        String[] names = {"Pace", "ORtg", "3P%", "2P%", "FT%", "ORB%", "DRB%", "AST%", "TS%", "eFG%", "FT/FG", "3PAr", "TOV%"};
        int[] compareePositions = averages.getCols(names);
        int[] ourPositions = getTeamCols(names);
        // Net multiply by a 100 to convert to a percent. IE a RelORTG of 10 means they were 10% better than the average
        // that year, which would be the greatest number of all time. 2016 GSW is 7.61, the record is 04 DAL at 8.94
        // (they had great offensive rebounders and shooters with Dirk at Center and glass crashers from the 2-4 and Nash at PG).
        // This is because 04 was the toughest year to score in post 3pt line besides 99, which was a lockout.
        for (int i = 0; i < names.length; i++) {
            teamCols.add("Rel" + names[i]);
            teamStats.add(Math.floor(10000 * (teamStats.get(ourPositions[i])/comparee.get(compareePositions[i]) - 1)) / 100);
        }
        // Make TOV% negative as lower is better, will do this with all the opponents stats too, as lower opponent efficiency is better.
        teamStats.set(teamStats.size() - 1, -teamStats.get(teamStats.size() - 1));
        String[] moreCols = {"DRtg", "OppeFG%", "OppFT/FG", "OppTOV%"};
        String[] moreColEquivalents = {"ORtg", "eFG%", "FT/FG", "TOV%"};
        String[] newNames = {"RelDRtg", "RelOpeFG", "RelOpFTR", "RelOpTOV"};
        ourPositions = getTeamCols(moreCols);
        int[] morePositions = averages.getCols(moreColEquivalents);
        for (int i = 0; i < ourPositions.length; i++) {
            teamCols.add(newNames[i]);
            teamStats.add(Math.floor(10000 * (teamStats.get(ourPositions[i])/comparee.get(morePositions[i]) - 1)) / -100);
        }
        // Same as above with TOV%
        teamStats.set(teamStats.size() - 1, -teamStats.get(teamStats.size() - 1));
    }

    // Adjust all the stats to per 100 poss, and also add in TS%.
    public void per100ize() {
        int minutesI = teamCols.indexOf("MP"), paceI = teamCols.indexOf("Pace");
        double adjustmentFactor = 24000.0 / (teamStats.get(minutesI) * teamStats.get(paceI));
        int[] positions = getTeamCols(new String[]{"FG", "FGA", "3P", "3PA", "2P", "2PA", "FT", "FTA", "AST", "BLK", "TOV"});
        for (int position : positions)
            teamStats.set(position, Math.floor(100 * teamStats.get(position) * adjustmentFactor) / 100);
        teamCols.add("TS%");
        teamStats.add(Math.floor(5000 * ((3 * teamStats.get(positions[2]) + 2 * teamStats.get(positions[4]) +
            teamStats.get(positions[6])) / (teamStats.get(positions[1]) + 0.44 * teamStats.get(positions[7])))) / 10000);
        teamCols.add("AST%");
        teamStats.add(Math.floor(10000 * teamStats.get(positions[8]) / teamStats.get(positions[0])) / 10000);
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
