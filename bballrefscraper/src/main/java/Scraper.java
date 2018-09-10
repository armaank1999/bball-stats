import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scraper {
    //<editor-fold desc="Massive list of constants">
    // The common portion of the url from everywhere we read from.
    public static final String baseTeamUrl = "https://www.basketball-reference.com/teams/";

    // A representation of the colNames of each respective table.
    private static final String[] advancedCols = {"Age", "G", "MP", "PER", "TS%", "3PAr", "FTr", "ORB%", "DRB%", "TRB%",
        "AST%", "STL%", "BLK%", "TOV%", "USG%", "OWS", "DWS", "WS", "WS/48", "OBPM", "DBPM", "BPM", "VORP"};
    // Pre 1974 advanced stats (post then we get ORB/DRB and BPM). Pre 1964 no AST% (since there's no OFFICIAL pace stat
    // even though estimate is there) either and pre 1952 no minutes, but ignoring the former for now and the latter forever.
    private static final String[] advancedColsOld = {"Age", "G", "MP", "PER", "TS%", "3PAr", "FTr", "TRB%",
        "AST%", "STL%", "BLK%", "TOV%", "USG%", "OWS", "DWS", "WS", "WS/48"};
    private static final String[] per100Cols = {"Age", "G", "GS", "MP", "FG", "FGA", "FG%", "3P", "3PA", "3P%", "2P", "2PA", "2P%",
        "FT", "FTA", "FT%", "ORB", "DRB", "TRB", "AST", "STL", "BLK", "TOV", "PF", "PTS", "ORtg", "DRtg"};
    // Only apply for post 2000 seasons.
    private static final String[] onOffCols = {"%MP", "OoTmEFG%", "OoORB%", "OoDRB%", "OoTRB%", "OoTmAST%", "OoTmSTL%",
        "OoTmBLK%", "OoTmTOV%", "OoTmPace", "OoORtg", "OoOpEFG%", "OoOpORB%", "OoOpDRB%", "OoOpTRB%", "OoOpAST%", "OoOpSTL%",
        "OoOpBLK%", "OoOpTOV%", "OoOpPace", "OoDRtg", "OoNtEFG%", "OoNtORB%", "OoNtDRB%", "OoNtTRB%", "OoNtAST%", "OoNtSTL%",
        "OoNtBLK%", "OoNtTOV%", "OoNtPace", "OoNtRtg"};

    // Cols from each table that are not needed. Per 100 also has duplicate cols with advanced, so those are included.
    private static final String[] advancedIgnorees = {"VORP", "DBPM", "OBPM", "DWS", "OWS", "TRB%", "PER"};
    private static final String[] per100Ignorees = {"PF", "TRB", "ORB", "DRB", "MP", "GS", "G", "Age"};
    private static final String[] onOffIgnorees = {"OoNtRtg", "OoNtPace", "OoNtTOV%", "OoNtEFG%", "OoNtBLK%", "OoNtSTL%",
        "OoNtAST%", "OoNtTRB%", "OoNtDRB%", "OoNtORB%", "OoNtEFG%", "OoOpBLK%", "OoOpSTL%", "OoOpAST%", "OoOpTRB%",
        "OoOpDRB%", "OoOpORB%", "OoOpPace", "OoTRB%", "OoTmBLK%", "OoTmSTL%", "OoTmAST%", "OoTmPace"};
    private static final String[] topTeamTableIgnorees = {"STL", "TRB", "DRB", "ORB"};
    private static final String[] bottomTeamTableIgnorees = {"FTr"};
    private static final String[] teamRepeats = {"eFG%", "TOV%", "FT/FGA"};
    private static final String[] teamRenames = {"OppeFG%", "OppTOV%", "OppFT/FG"};
    //</editor-fold>

    // Want the averages of allYears to be a global variable, and it is static as there's only one overarching average file.
    private static SeasonList allYears;

    public static void main(String[] args) throws Exception {
        allYears = SeasonList.readSeasonList("years.csv");
//        allYears.saveFile("parsed");
        parseSeason("GSW", 2016);
//        parseSeason("CLE", 2009);
//        parseSeason("LAL", 2009);
//        parseSeason("DET", 2004);
//        parseSeason("OKC", 2018);
//        parseSeason("BOS", 2018);
    }

    private static void parseSeason(String team, int year) throws Exception {
        TeamSeason parsedInfo = new TeamSeason(team, year);
        readSeasonLink(parsedInfo);
        parsedInfo.printAllInfo();
//        readOnOffLink(parsedInfo);
//        parsedInfo.addAdjustments();
//        parsedInfo.saveFile();
    }

    private static void readOnOffLink(TeamSeason szn) throws Exception {
        // First find the url and table we want. Find the real content of the table with our helper.
        // Then call our add rows helper to add the rows and remove the ones we don't want.
        String ofOffLink = String.format("%s%s/%s/on-off", baseTeamUrl, szn.team, szn.year);
        Document statsDoc = Jsoup.connect(ofOffLink).get();
        Node blob = statsDoc.selectFirst("[role=main]").selectFirst("div#all_on_off");
        addOnOffRows(szn, trSeasons(blob.childNode(blob.childNodeSize() - 2).outerHtml().split("[\\r\\n]+")));
        szn.deleteCols(onOffIgnorees);
    }

    private static void readSeasonLink(TeamSeason szn) throws Exception {
        // First find the url and table we want. Find the real content of the table(s) with our helper.
        // Then call our add rows helper to add the rows and remove the ones we don't want.
        String seasonLink = String.format("%s%s/%s.html", baseTeamUrl, szn.team, szn.year);
        Document statsDoc = Jsoup.connect(seasonLink).get();
        Element blob = statsDoc.selectFirst("[role=main]");
        Node allFactors = blob.selectFirst("div#all_team_and_opponent");
        String[] allFactorsTable = allFactors.childNode(allFactors.childNodeSize() - 2).outerHtml().split("[\\r\\n]+");
        Node moreFactors = blob.selectFirst("div#all_team_misc");
        String[] moreFactorsTable = moreFactors.childNode(moreFactors.childNodeSize() - 2).outerHtml().split("[\\r\\n]+");
        addRows(szn, tableRows(blob, "div#all_advanced"), advancedCols);
        szn.deleteCols(advancedIgnorees);
        // TODO: Add back in once missing col issue is fixed (i.e. no 3pt%)
        // addRows(szn, tableRows(blob, "div#all_per_poss"), per100Cols);
        // szn.deleteCols(per100Ignorees);
        addTeamInfo(szn, allFactorsTable, moreFactorsTable);
//        szn.addRelativeInfo(allYears);
    }

    // Get only the lines that are in the tbody of a comment blob.
    private static String[] tbodySeasons(String[] allSeasons) {
        int firstRow = searchFromFront("<tbody>", allSeasons) + 1;
        int numPlayers = searchFromEnd("</tbody>", allSeasons) - firstRow;
        String[] playerSeasons = new String[numPlayers];
        System.arraycopy(allSeasons, firstRow, playerSeasons, 0, numPlayers);
        return playerSeasons;
    }

    // Get only the lines that are in the trs of a comment blob.
    private static String[] trSeasons(String[] allSeasons) {
        // First get all the rows that are actually in the table.
        int lastIgnoredRow = searchFromFront("<tr ><th", allSeasons);
        int numPlayers = searchFromEnd("</td></tr>", allSeasons) - lastIgnoredRow + 1;
        String[] playerSeasons = new String[numPlayers];
        System.arraycopy(allSeasons, lastIgnoredRow, playerSeasons, 0, numPlayers);
        // Now remove all the additional headers that are in between actual rows. Don't need to check last three
        List<String> returnee = new ArrayList<>(Arrays.asList(playerSeasons));
        for (int i = returnee.size() - 4; i > 0; i--) {
            if (returnee.get(i).startsWith(" ") || returnee.get(i).startsWith("<tr class=\"thead\"><td")) {
                returnee.remove(i);
            }
        }
        return returnee.toArray(new String[0]);
    }

    // helpers to find line numbers so the rest can be ignored, and then the relevant rows are parsed
    private static int searchFromFront(String pattern, String[] rows) {
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].contains(pattern))
                return i;
        }
        return -1;
    }

    private static int searchFromEnd(String pattern, String[] rows) {
        for (int i = rows.length - 1; i > 0; i--) {
            if (rows[i].contains(pattern))
                return i;
        }
        return -1;
    }

    // functions to take the rows, parse them properly, and then add their info to the szn
    private static void addRows(TeamSeason szn, String[] years, String[] colNames) {
        String[] names = new String[years.length];
        double[][] table = new double[years.length][];
        for (int i = 0; i < years.length; i++) {
            // TODO: figure out a way to avoid this method of adding spaces and then calling relevantChildren
            // as that messes up when cols are actually empty (e.g. Ben Wallace 3p%)
            Element row = Jsoup.parse(addSpaces(years[i]));
            String[] colVals = relevantChildren(row.outerHtml().split("<body>\n {2}")[1].split("\n </body>")[0]);
            String rowName = colVals[0];
            double[] colAsNums = new double[colVals.length - 1];
            for (int j = 0; j < colAsNums.length; j++) {
                colAsNums[j] = Double.parseDouble(colVals[j + 1]);
            }
            names[i] = rowName;
            table[i] = colAsNums;
        }
        szn.addPlayers(colNames, names, table);
    }

    private static void addOnOffRows(TeamSeason szn, String[] rows) {
        // Each player row is 3 high - on, off, difference. We only want the difference for now, except for name which is in 1.
        String[] names = new String[rows.length / 3];
        double[][] colVals = new double[rows.length / 3][];
        for (int i = 0; i < rows.length; i += 3) {
            // Assign the name, which is in the hyperlink tag (so in the tag after the initial link, which ends in .html)
            names[i / 3] = rows[i].split(".*html\">")[1].split("<")[0];
            List<String> allSplitRows = new ArrayList<String>(Arrays.asList(rows[i + 2].split("<.*?>")));
            for (int j = allSplitRows.size() - 1; j >= 0; j--) {
                // Remove the blank columns that are in between the meaningful ones.
                if (allSplitRows.get(j).equals("")) {
                    allSplitRows.remove(j);
                }
            }
            // Remove the first column which is just a pointless text tag.
            allSplitRows.remove(0);
            String[] realValues = allSplitRows.toArray(new String[0]);
            double[] trulyParsedValues = new double[realValues.length];
            // the first one is represented as a percentage so ignore the percentage sign
            trulyParsedValues[0] = Double.parseDouble(realValues[0].substring(0, realValues[0].length() - 1));
            // If it starts with a +, remove it, else keep the -
            for (int k = 1; k < realValues.length; k++) {
                String parsee = realValues[k];
                if (parsee.startsWith("+")) {
                    trulyParsedValues[k] = Double.parseDouble(parsee.substring(1));
                } else {
                    trulyParsedValues[k] = Double.parseDouble(parsee);
                }
            }
            colVals[i / 3] = trulyParsedValues;
        }
        szn.addPlayers(onOffCols, names, colVals);
    }

    // Adds spaces to a row's columns to allow the later parsing by split to be easier once JSoup's parser removes junk.
    private static String addSpaces(String row) {
        char[] letters = row.toCharArray();
        int j = 0; // Position in row
        for (int i = 0; i < letters.length; i++, j++) {
            if (letters[i] == '<' && i > 0 && letters[i - 1] != '>') {
                row = row.substring(0, j) + " " + row.substring(j);
                j += 1;
            }
        }
        return row;
    }

    // Gets an array of all attributes that the row has. First is rank, which we ignore. Second is name, which is
    // wrapped in a href so it's extracted differently from the others
    private static String[] relevantChildren(String blob) {
        String[] children = String.join("", blob.split("\n")).split("<.*?>");
        String name = children[1].substring(0, children[1].length() - 1);
        StringBuilder allChildren = new StringBuilder();
        for (int i = 2; i < children.length; i++) {
            allChildren.append(children[i]);
        }
        String[] temp = allChildren.toString().split(" +");
        String[] returnee = new String[temp.length + 1];
        System.arraycopy(temp, 0, returnee, 1, temp.length);
        returnee[0] = name;
        return returnee;
    }

    // Finds all the rows of a table with a certain identifier by seeking out its commented out version
    // and then finding only the important rows and splitting them into an array.
    private static String[] tableRows(Element container, String tableSelector) {
        Node blob = container.selectFirst(tableSelector);
        return tbodySeasons(blob.childNode(blob.childNodeSize() - 2).outerHtml().split("[\\r\\n]+"));
    }

    // Takes the selected top and bottom rows from their comment blobs, parses them, and adds it to the team season.
    private static void addTeamInfo(TeamSeason szn, String[] topRows, String[] bottomRows) {
        int firstUsedRow = searchFromFront("<tr>", topRows) + 3;
        int numRows = searchFromEnd("  </tr>", topRows) - firstUsedRow - 2;
        String[] topLabelRows = new String[numRows];
        System.arraycopy(topRows, firstUsedRow, topLabelRows, 0, numRows);
        firstUsedRow = searchFromFront("<tr>", bottomRows) + 8;
        numRows = searchFromEnd("  </tr>", bottomRows) - firstUsedRow - 2;
        String[] bottomLabelRows = new String[numRows];
        System.arraycopy(bottomRows, firstUsedRow, bottomLabelRows, 0, numRows);
        String[] topLabels = new String[topLabelRows.length];
        String[] bottomLabels = new String[bottomLabelRows.length];
        for (int i = 0; i < topLabels.length; i++)
            topLabels[i] = topLabelRows[i].split("<*+ >")[1].split("<")[0];
        for (int i = 0; i < bottomLabels.length; i++)
            bottomLabels[i] = bottomLabelRows[i].split("<*+ >")[1].split("<")[0];

        String topTeamVal = topRows[searchFromEnd("Team/G", topRows)];
        String bottomTeamVal = bottomRows[searchFromFront("<tr >", bottomRows)];
        String[] topSplitArr = String.join("  ", topTeamVal.split("<.*?>")).split("  +");
        double[] topRelevantArr = new double[topSplitArr.length - 4];
        for (int i = 0; i < topRelevantArr.length; i++)
            topRelevantArr[i] = Double.parseDouble(topSplitArr[i+2]);
        String[] bottomSplitArr = String.join("  ", bottomTeamVal.split("<.*?>")).split("  +");
        double[] bottomRelevantArr = new double[bottomSplitArr.length - 10];
        for (int i = 0; i < bottomRelevantArr.length; i++)
            bottomRelevantArr[i] = Double.parseDouble(bottomSplitArr[i+8]);

        szn.addTeamAttributes(topLabels, topRelevantArr);
        szn.deleteTeamCols(topTeamTableIgnorees);
        szn.addTeamAttributes(bottomLabels, bottomRelevantArr);
        szn.deleteTeamCols(bottomTeamTableIgnorees);
        szn.renameTeamCols(teamRepeats, teamRenames);
        szn.per100ize();
        szn.addRelativeInfo(allYears);
    }
}
