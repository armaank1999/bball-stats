import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scraper {
    // CSV delimiters
    public static final String CSV_SPLIT_BY = ",";
    public static final String NEW_LINE = "\n";
    // The common portion of the url from everywhere we read from.
    public static final String baseTeamUrl = "https://www.basketball-reference.com/teams/";

    // A representation of the colNames of each respective table.
    private static final String[] advancedCols = {"Age", "G", "MP", "PER", "TS%", "3PAr", "FTr", "ORB%", "DRB%", "TRB%",
            "AST%", "STL%", "BLK%", "TOV%", "USG%", "OWS", "DWS", "WS", "WS/48", "OBPM", "DBPM", "BPM", "VORP"};
    private static final String[] per100Cols = {"Age", "G", "GS", "MP", "FG", "FGA", "FG%", "3P", "3PA", "3P%", "2P", "2PA", "2P%",
            "FT", "FTA", "FT%", "ORB", "DRB", "TRB", "AST", "STL", "BLK", "TOV", "PF", "PTS", "ORtg", "DRtg"};
    private static final String[] onOffCols = {"%MP", "OoTmEFG%", "OoORB%", "OoDRB%", "OoTRB%", "OoTmAST%", "OoTmSTL%",
            "OoTmBLK%", "OoTmTOV%", "OoTmPace", "OoORtg", "OoOpEFG%", "OoOpORB%", "OoOpDRB%", "OoOpTRB%", "OoOpAST%", "OoOpSTL%",
            "OoOpBLK%", "OoOpTOV%", "OoOpPace", "OoDRtg", "OoNtEFG%", "OoNtORB%", "OoNtDRB%", "OoNtTRB%", "OoNtAST%", "OoNtSTL%",
            "OoNtBLK%", "OoNtTOV%", "OoNtPace", "OoNtRtg"};

    // Cols from each table that are not needed. Per 100 also has duplicate cols with advanced, so those are included.
    private static final String[] advancedIgnorees = {"VORP", "DBPM", "OBPM", "DWS", "OWS", "TRB%", "PER"};
    private static final String[] per100Ignorees = {"PF", "TRB", "ORB", "DRB", "MP", "GS", "G", "Age"};
    private static final String[] onOffIgnorees = {"OoNtRtg", "OoNtPace", "OoNtTOV%", "OoNtEFG%", "OoNtBLK%", "OoNtSTL%",
            "OoNtAST%", "OoNtTRB%", "OoNtDRB%", "OoNtORB%", "OoNtEFG%", "OoOpBLK%", "OoOpSTL%", "OoOpAST%", "OoOpTRB%",
            "OoOpDRB%", "OoOpORB%", "OoOpPace", "OoTmTRB%", "OoTmBLK%", "OoTmSTL%", "OoTmAST%", "OoTmPace"};

    public static void main(String[] args) throws Exception {
        parseSeason("BOS", 2018);
        // SeasonList allYears = SeasonList.seasonFromFile("years.csv");
    }

    private static void parseSeason(String team, int year) throws Exception {
        TeamSeason parsedInfo = new TeamSeason(team, year);
        readSeasonLink(parsedInfo);
        readOnOffLink(parsedInfo);
        parsedInfo.addAdjustments();
        //parsedInfo.printAllInfo();
        parsedInfo.saveFile();
    }

    private static void readOnOffLink(TeamSeason szn) throws Exception {
        // First find the url and table we want. Find the real content of the table with our helper.
        // Then call our add rows helper to add the rows and remove the ones we don't want.
        String ofOffLink = baseTeamUrl + szn.team + "/" + szn.year + "/on-off";
        Document statsDoc = Jsoup.connect(ofOffLink).get();
        Node onOffBlob = statsDoc.selectFirst("[role=main]").selectFirst("div#all_on_off");
        addOnOffRows(szn, trSeasons(onOffBlob.childNode(onOffBlob.childNodeSize() - 2).outerHtml().split("[\\r\\n]+")));
        szn.deleteCols(onOffIgnorees);
    }

    private static void readSeasonLink(TeamSeason szn) throws Exception {
        // First find the url and table we want. Find the real content of the table(s) with our helper.
        // Then call our add rows helper to add the rows and remove the ones we don't want.
        String seasonLink = baseTeamUrl + szn.team + "/" + szn.year + ".html";
        Document statsDoc = Jsoup.connect(seasonLink).get();
        Element statBlob = statsDoc.selectFirst("[role=main]");
        addRows(szn, tableRows(statBlob, "div#all_advanced"), advancedCols);
        szn.deleteCols(advancedIgnorees);
        // addRows(szn, tableRows(statBlob, "div#all_per_poss"), per100Cols);
        // szn.deleteCols(per100Ignorees);
    }

    // Get only the lines that are in the tbody of a comment blob.
    private static String[] tbodySeasons(String[] allSeasons) {
        int firstRow = tbodyLocation(allSeasons) + 1;
        int numPlayers = tbodyEndLocation(allSeasons) - firstRow;
        String[] playerSeasons = new String[numPlayers];
        System.arraycopy(allSeasons, firstRow, playerSeasons, 0, numPlayers);
        return playerSeasons;
    }

    // Get only the lines that are in the trs of a comment blob.
    private static String[] trSeasons(String[] allSeasons) {
        // First get all the rows that are actually in the table.
        int lastIgnoredRow = trhLocation(allSeasons);
        int numPlayers = trhEndLocation(allSeasons) - lastIgnoredRow + 1;
        String[] playerSeasons = new String[numPlayers];
        System.arraycopy(allSeasons, lastIgnoredRow, playerSeasons, 0, numPlayers);
        // Now remove all the additional headers that are in between actual rows.
        List<String> removableSeasonList = new ArrayList<String>(Arrays.asList(playerSeasons));
        for (int i = removableSeasonList.size() - 4; i > 0; i--) {
            if (removableSeasonList.get(i).startsWith(" ") || removableSeasonList.get(i).startsWith("<tr class=\"thead\"><td")) {
                removableSeasonList.remove(i);
            }
        }
        return removableSeasonList.toArray(new String[0]);
    }

    // helpers to find line numbers so the rest can be ignored, and then the relevant rows are parsed
    private static int tbodyLocation(String[] htmlBlob) {
        for (int i = 0; i < htmlBlob.length; i++) {
            if (htmlBlob[i].contains("<tbody>")) {
                return i;
            }
        }
        return -1;
    }

    private static int tbodyEndLocation(String[] htmlBlob) {
        for (int i = htmlBlob.length - 1; i > 0; i--) {
            if (htmlBlob[i].contains("</tbody>")) {
                return i;
            }
        }
        return -1;
    }

    private static int trhLocation(String[] htmlBlob) {
        for (int i = 0; i < htmlBlob.length; i++) {
            if (htmlBlob[i].startsWith("<tr ><th")) {
                return i;
            }
        }
        return -1;
    }

    private static int trhEndLocation(String[] htmlBlob) {
        for (int i = htmlBlob.length - 1; i > 0; i--) {
            if (htmlBlob[i].contains("</td></tr>")) {
                return i;
            }
        }
        return -1;
    }

    // reads a text file for its line
    private static List<String> readFile(String fileName) throws Exception {
        String currLine;
        ArrayList<String> blobs = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        while ((currLine = br.readLine()) != null) {
            if (currLine.length() > 0) {
                blobs.add(currLine);
            }
        }
        return blobs;
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
        // Each player row is 3 high - on, off, difference. We only want the difference for now, except for name which is in on.
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

    // Gets an array of all attributes that the row has.
    private static String[] relevantChildren(String blob) {
        String[] lines = blob.split("\n");
        StringBuilder allLines = new StringBuilder();
        for (String line : lines) {
            allLines.append(line);
        }
        String[] children = allLines.toString().split("<.*?>");
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
    // and then finding only the important rows and splitting them into a list.
    private static String[] tableRows(Element container, String tableSelector) {
        Node comment = container.selectFirst(tableSelector);
        return tbodySeasons(comment.childNode(comment.childNodeSize() - 2).outerHtml().split("[\\r\\n]+"));
    }
}
