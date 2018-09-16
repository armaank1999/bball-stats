import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Arrays;

public class TeamSeason {
    // Scaling constants for new stats, which should be addable to WS/48, so adjust magnitude accordingly.
    private final static double minAdjCoeff = 0.02;
    private final static double onOffCoeff = 0.05;
    private final static double rebCoeff = 0.002;
    // The base URL for all pages
    private static final String baseUrl = "https://www.basketball-reference.com/teams/";

    private final String team;
    public final int year;
    private final List<String> playerCols = new ArrayList<>();
    private final Map<String, ArrayList<Double>> playerSeasons = new LinkedHashMap<>();
    private final List<String> teamCols = new ArrayList<>();
    private final List<Double> teamStats = new ArrayList<>();
    private final List<String> oppCols = new ArrayList<>();
    private final List<Double> oppStats = new ArrayList<>();

    public TeamSeason(String t, int y) {
        year = y;
        team = t;
    }

    public String url(String ending) {
        return baseUrl + team + "/" + year + ending;
    }

    public void printAllInfo() {
        System.out.printf("Parsed from %s\n", url(".html"));
        if (teamCols.size() > 0) {
            System.out.println("Team Stats: ");
            for (String colName : teamCols)
                System.out.printf("%-9s", colName);
            System.out.println();
            for (Double val : teamStats)
                System.out.printf("%-9s", val);
            System.out.println();
        }
        if (oppCols.size() > 0) {
            System.out.println("Opp Stats:  ");
            for (String colName : oppCols)
                System.out.printf("%-9s", colName);
            System.out.println();
            for (Double val : oppStats)
                System.out.printf("%-9s", val);
            System.out.println();
        }
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
        FileWriter output = new FileWriter("playerOutput/" + year + team + ".csv");
        StringBuilder fileValue = new StringBuilder("Name,");
        fileValue.append(String.join(",", playerCols)).append("\n").append(String.join("\n", rowCSVs()));
        output.append(fileValue).flush();
        output.close();
        if (teamCols.size() > 0) {
            FileWriter teamOutput = new FileWriter("relativeOutput/" + year + team + ".csv");
            StringBuilder teamValue = new StringBuilder();
            teamValue.append(String.join(",", teamCols)).append("\n");
            for (double stat : teamStats)
                teamValue.append(stat).append(",");
            teamValue.setLength(teamValue.length() - 1);
            teamOutput.append(teamValue).flush();
            teamOutput.close();
        }
    }

    public void addPlayerStats(String[] newColNames, String[] names, double[][] colVals) {
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

    public void addOppAttributes(String[] newColNames, double[] vals) {
        oppCols.addAll(Arrays.asList(newColNames));
        for (double val : vals)
            oppStats.add(val);
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

    private int[] getOppCols(String[] names) {
        int[] returnee = new int[names.length];
        for (int i = 0; i < names.length; i++)
            returnee[i] = oppCols.indexOf(names[i]);
        return returnee;
    }

    public void deleteBlankCols() {
        while (deleteCol(""));
    }

    public void deleteCols(String[] cols) {
        deleteBuggers();
        for (String name : cols) deleteCol(name);
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

    public void deleteTeamCols(String[] cols) {
        for (String name : cols)
            deleteTeamCol(name);
    }

    public void deleteOppCols(String[] cols) {
        for (String name : cols)
            deleteOppCol(name);
    }

    private void deleteTeamCol(String colName) {
        int index = teamCols.lastIndexOf(colName);
        if (index != -1) {
            teamCols.remove(index);
            teamStats.remove(index);
        }
    }

    private void deleteOppCol(String colName) {
        int index = oppCols.lastIndexOf(colName);
        if (index != -1) {
            oppCols.remove(index);
            oppStats.remove(index);
        }
    }

    // Deletes players who have too few cols, stopgap until we can 0 fill them.
    private void deleteBuggers() {
        int len = playerSeasons.values().iterator().next().size();
        List<String> incompletes = new ArrayList<>();
        for (String name : playerSeasons.keySet())
            if (playerSeasons.get(name).size() < len)
                incompletes.add(name);
        for (String name : incompletes)
            playerSeasons.remove(name);
    }

    // Takes a column and adjusts each value so that the weighted sum is 0, then round to 4 decimal places. Necessary so the
    // total sum of AdjWS = total sum WS = team expected wins.
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

    // Takes a column and divides each value by sqrt(stddev) to semi adjust gaps that are too large, then divides by 10
    // afterwards because we want the end results to be slightly smaller than WS/48.
    private void semiNormalize(int colPos) {
        int weightPos = playerCols.indexOf("%MP");
        double stdDev = 0;
        for (ArrayList<Double> player : playerSeasons.values())
            stdDev += player.get(colPos) * player.get(colPos) * player.get(weightPos);
        stdDev = Math.pow(stdDev/500, 0.25);
        for (ArrayList<Double> player : playerSeasons.values())
            player.set(colPos, Math.floor(1000 * player.get(colPos) / stdDev) / 10000);
    }

    // Adds column based on the other adjustment methods and add a new column for the sum of them.
    // RBAdj: Subtract a multiple of individual ORB/DRB% and add in the on-off effect that player has on
    // the team rebounding wise to gauge actual impact/punish stat-padding, then normalize the column.
    // OOAdj: Add/subtract points based on how much better the team/opponent's offensive ratings are. Weigh defensive impact more
    // as that is less captured by the other baseline advanced stats - number should be between 1 and 2.
    // MinAdj: Give points based on total games played minutes per game as clearly players who play more MPG are better
    // but WS/48 ignores that. Weight other changes based on sqrt games and %MP * (1 - %MP) - stddev for binomial.
    // AdjWS/48: Sum of the above plus original WS/48
    // AdjWS: AdjWS/48 * minutes/48
    public void addAdjustments() {
        int minutesI = playerCols.indexOf("MP"), percentI = playerCols.indexOf("%MP"), gamesI = playerCols.indexOf("G"),
            offOOI = playerCols.indexOf("OoORtg"), defOOI = playerCols.indexOf("OoDRtg"), orbI = playerCols.indexOf("ORB%"),
            drbI = playerCols.indexOf("DRB%"), netORBI = playerCols.indexOf("OoORB%"), netDRBI = playerCols.indexOf("OoDRB%");
        for (List<Double> row : playerSeasons.values()) {
            double GP = row.get(gamesI), MP = row.get(minutesI), P = row.get(percentI);
            double weight = Math.sqrt(GP) * P * (100 - P) / 22500;
            row.add((row.get(offOOI) - 1.5 * row.get(defOOI)) * onOffCoeff * weight);
            row.add(minAdjCoeff * (0.5 * Math.log(GP) + Math.log(Math.max(MP / GP - 5, 1)) + Math.sqrt((30 + MP) / (GP + 2))));
            row.add((row.get(netORBI) + row.get(netDRBI) - 0.75 * row.get(orbI) - 0.75 * row.get(drbI)) * rebCoeff * weight);
        }
        normalize(playerCols.size());
        semiNormalize(playerCols.size());
        playerCols.add("OoAdj");
        normalize(playerCols.size());
        playerCols.add("MinAdj");
        normalize(playerCols.size());
        playerCols.add("RBAdj");
        int wsI = playerCols.indexOf("WS/48"), rbI = playerCols.indexOf("RBAdj"), ooI = playerCols.indexOf("OoAdj"), minI = playerCols.indexOf("MinAdj");
        for (List<Double> row : playerSeasons.values()) {
            row.add((double) Math.round((row.get(wsI) + row.get(rbI) + row.get(ooI) + row.get(minI)) * 10000) / 10000);
            row.add((double) Math.round(row.get(playerCols.size()) * row.get(minutesI) * 125 / 6) / 1000);
        }
        playerCols.add("AdjWS/48");
        playerCols.add("AdjWS");
    }

    // For pre +- seasons, just add minAdj. No need for AdjWS/48 and AdjWS since no plus minus data means we can't get the others
    public void addMinAdj() {
        int minutesI = playerCols.indexOf("MP"), gamesI = playerCols.indexOf("G");
        for (List<Double> row : playerSeasons.values()) {
            double GP = row.get(gamesI), MP = row.get(minutesI);
            row.add(minAdjCoeff * (0.5 * Math.log(GP) + Math.log(Math.max(MP / GP - 5, 1)) + Math.sqrt((30 + MP) / (GP + 2))));
        }
        normalize(playerCols.size());
        playerCols.add("MinAdj");
    }

    // Compares the team's stats to the league average of that season to account for era changes. Which defense is actually the best
    // given the era, not just which happens to be in an era of handcheck?
    public void addRelativeInfo(SeasonList averages) {
        List<Double> comparee = averages.getYear(year);
        String[] names = {"Pace", "ORtg", "3P%", "2P%", "FT%", "ORB%", "DRB%", "AST%", "TS%", "eFG%", "FT/FG", "3PAr", "TOV%"};
        int[] compareePositions = averages.getCols(names);
        int[] ourPositions = getTeamCols(names);
        // Net multiply by a 100 to convert to a percent. IE a RelORTG of 10 means they were 10% better than the average
        // that year, which would be the greatest number of all time. 2016 GSW is 7.61, the record is 04 DAL at 8.94
        // (they had great offensive rebounders and shooters with Dirk at Center and glass crashers from the 2-4 and Nash at PG).
        // This is because 04 was the toughest year to score post 3pt line besides 99 == lockout. 2016 spurs were a top 15 defense of all time.
        // TODO: fix net DRB% to scale like net ORB%
        for (int i = 0; i < names.length; i++) {
            if (ourPositions[i] != -1) {
                teamCols.add("Rel" + names[i]);
                teamStats.add(Math.floor(10000 * (teamStats.get(ourPositions[i]) / comparee.get(compareePositions[i]) - 1)) / 100);
            }
        }
        // Make TOV% negative as lower is better, will do this with all the opponents stats too, as lower opponent efficiency is better.
        // Question: should this be -(team/comparee - 1) or comparee/team - 1? The bleacher report article uses the latter for best defenses
        // of all time (https://bleacherreport.com/articles/2185159-ranking-the-nbas-20-best-defenses-of-all-time)
        if (ourPositions[ourPositions.length - 1] != -1) teamStats.set(teamStats.size() - 1, -teamStats.get(teamStats.size() - 1));
        String[] moreCols = {"DRtg", "OppeFG%", "OppFT/FG", "OppTOV%"};
        String[] moreColEquivalents = {"ORtg", "eFG%", "FT/FG", "TOV%"};
        String[] newNames = {"RelDRtg", "RelOpeFG", "RelOpFTR", "RelOpTOV"};
        ourPositions = getTeamCols(moreCols);
        int[] morePositions = averages.getCols(moreColEquivalents);
        for (int i = 0; i < ourPositions.length; i++) {
            if (ourPositions[i] != -1) {
                teamCols.add(newNames[i]);
                teamStats.add(Math.floor(10000 * (teamStats.get(ourPositions[i]) / comparee.get(morePositions[i]) - 1)) / -100);
            }
        }
        // Same as above with TOV%
        if (ourPositions[3] != -1) teamStats.set(teamStats.size() - 1, -teamStats.get(teamStats.size() - 1));
        // Add in oppCols now that we get the third row of the table too.

    }

    // Adjust all the stats to per 100 poss, and also add in TS% and AST%.
    public void per100ize() {
        double adjustmentFactor;
        // No team minutes pre 1965, so just assume no overtime.
        if (year < 1965) adjustmentFactor = 100.0 / teamStats.get(teamCols.indexOf("Pace"));
        else adjustmentFactor = 24000.0 / (teamStats.get(teamCols.indexOf("MP")) * teamStats.get(teamCols.indexOf("Pace")));
        String[] attributes = {"FG", "FGA", "3P", "3PA", "2P", "2PA", "FT", "FTA", "TRB", "AST", "BLK", "TOV"};
        int[] positions = getTeamCols(attributes);
        for (int position : positions)
            if (position != -1) teamStats.set(position, Math.floor(100 * teamStats.get(position) * adjustmentFactor) / 100);
        teamCols.add("TS%");
        teamCols.add("AST%");
        if (year > 1979) teamStats.add(Math.floor(5000 * ((3 * teamStats.get(positions[2]) + 2 * teamStats.get(positions[4]) +
            teamStats.get(positions[6])) / (teamStats.get(positions[1]) + 0.44 * teamStats.get(positions[7])))) / 10000);
        else teamStats.add(Math.floor(5000 * ((2 * teamStats.get(positions[0]) + teamStats.get(positions[6])) /
            (teamStats.get(positions[1]) + 0.44 * teamStats.get(positions[7])))) / 10000);
        teamStats.add(Math.floor(10000 * teamStats.get(positions[9]) / teamStats.get(positions[0])) / 10000);
        if (oppCols.size() > 0) {
            positions = getOppCols(attributes);
            for (int position : positions)
                if (position != -1) oppStats.set(position, Math.floor(100 * oppStats.get(position) * adjustmentFactor) / 100);
            oppCols.add("TS%");
            oppCols.add("AST%");
            if (year > 1979) oppStats.add(Math.floor(5000 * ((3 * oppStats.get(positions[2]) + 2 * oppStats.get(positions[4]) +
                oppStats.get(positions[6])) / (oppStats.get(positions[1]) + 0.44 * oppStats.get(positions[7])))) / 10000);
            else oppStats.add(Math.floor(5000 * ((2 * oppStats.get(positions[0]) + oppStats.get(positions[6])) /
                (oppStats.get(positions[1]) + 0.44 * oppStats.get(positions[7])))) / 10000);
            oppStats.add(Math.floor(10000 * oppStats.get(positions[9]) / oppStats.get(positions[0])) / 10000);
        }
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
