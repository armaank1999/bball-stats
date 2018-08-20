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
    private static final String[] advancedRows = {"Age", "G", "MP", "PER", "TS%", "3PAr", "FTr", "ORB%", "DRB%", "TRB%",
            "AST%", "STL%", "BLK%", "TOV%", "USG%", "OWS", "DWS", "WS", "WS/48", "OBPM", "DBPM", "BPM", "VORP"};
    private static final String[] per100Rows = {"Age", "G", "GS", "MP", "FG", "FGA", "FG%", "3P", "3PA", "3P%", "2P", "2PA", "2P%",
            "FT", "FTA", "FT%", "ORB", "DRB", "TRB", "AST", "STL", "BLK", "TOV", "PF", "PTS", "ORtg", "DRtg"};
    private static final String[] onOffRows = {"%MP", "+-TmEFG%", "+-TmORB%", "+-TmDRB%", "+-TmAST%", "+-TmSTL%",
            "+-TmBLK%", "+-TmTOV%", "+-TmPace", "+-TmORtg", "+-OppEFG%", "+-OppORB%", "+-OppDRB%", "+-OppAST%", "+-OppSTL%",
            "+-OppBLK%", "+-OppTOV%", "+-OppPace", "+-OppORtg", "+-NetEFG%", "+-NetORB%", "+-NetDRB%", "+-NetAST%", "+-NetSTL%",
            "+-NetBLK%", "+-NetTOV%", "+-NetPace", "+-NetRtg"};
    // Rows from advanced that are not needed.
    private static final String[] advancedIgnorees = {"PER", "TRB%", "OWS", "DWS", "OBPM", "DBPM", "VORP"};
    // Includes the duplicates with advanced and the per 100 we don't want.
    private static final String[] per100Ignorees = {"Age", "G", "GS", "MP", "PF", "TRB", "ORB", "DRB"};
    // All the cols we don't want from the on-off table, since there are a ton.
    private static final String[] onOffIgnorees = {};

    public static final String baseTeamUrl = "https://www.basketball-reference.com/teams/";

    public static void main(String[] args) throws Exception {
//        readSeasonLink("CLE", 2009);
        readOnOffLink("LAL",2009);
    }

    private static void readOnOffLink(String team, int year) throws Exception {
        TeamSeason parsedInfo = new TeamSeason(year, team);
        String ofOffLink = baseTeamUrl + team + "/" + year + "/on-off";
        Document statsDoc = Jsoup.connect(ofOffLink).get();
        Node onOffBlob = statsDoc.selectFirst("[role=main]").selectFirst("div#all_on_off");
        String[] comment = trSeasons(onOffBlob.childNode(onOffBlob.childNodeSize()-2).outerHtml().split("[\\r\\n]+"));
        String[] names = new String[comment.length/3];
        double[][] colVals = new double[comment.length/3][];
        for (int i = 0; i < comment.length; i+=3) {
            names[i/3] = comment[i].split(".*html\">")[1].split("<")[0];
            List<String> allSplitRows = new ArrayList<String>(Arrays.asList(comment[i+2].split("<.*?>")));
            for (int j = allSplitRows.size() - 1; j >= 0; j--) {
                if (allSplitRows.get(j).equals("")) {
                    allSplitRows.remove(j);
                }
            }
            allSplitRows.remove(0);
            String[] realValues = allSplitRows.toArray(new String[0]);
            double[] trulyParsedValues = new double[realValues.length];
            trulyParsedValues[0] = Double.parseDouble(realValues[0].substring(0,realValues[0].length()-1));
            for (int k = 1; k < realValues.length; k++) {
                String parsee = realValues[k];
                if (parsee.startsWith("+")) {
                    trulyParsedValues[k] = Double.parseDouble(parsee.substring(1));
                } else {
                    trulyParsedValues[k] = Double.parseDouble(parsee);
                }
            }
            colVals[i/3] = trulyParsedValues;
        }
        parsedInfo.addPlayers(onOffRows, names, colVals);
        parsedInfo.printAllInfo();
        parsedInfo.saveFile();
    }

    private static void readSeasonLink(String team, int year) throws Exception {
        TeamSeason parsedInfo = new TeamSeason(year, team);
        String seasonLink = baseTeamUrl + team + "/" + year + ".html";
        Document statsDoc = Jsoup.connect(seasonLink).get();
        Element statBlob = statsDoc.selectFirst("[role=main]");
        // addRows(parsedInfo, tableRows(statBlob, "div#all_per_poss"), per100Rows);
        addRows(parsedInfo, tableRows(statBlob, "div#all_advanced"), advancedRows, advancedIgnorees);
        parsedInfo.printAllInfo();
        parsedInfo.saveFile();
    }

    // Get only the lines that are in the tbody of a comment blob.
    private static String[] tbodySeasons(String[] allSeasons) {
        int firstRow = tbodyLocation(allSeasons) + 1;
        int numPlayers = tbodyEndLocation(allSeasons) - firstRow;
        String[] playerSeasons = new String[numPlayers];
        System.arraycopy(allSeasons,firstRow, playerSeasons, 0, numPlayers);
        return playerSeasons;
    }

    // Get only the lines that are in the trs of a comment blob.
    private static String[] trSeasons(String[] allSeasons) {
        int lastIgnoredRow = trhLocation(allSeasons);
        int numPlayers = trhEndLocation(allSeasons) - lastIgnoredRow + 1;
        String[] playerSeasons = new String[numPlayers];
        System.arraycopy(allSeasons, lastIgnoredRow, playerSeasons, 0, numPlayers);
        List<String> removableSeasonList = new ArrayList<String>(Arrays.asList(playerSeasons));
        for (int i = removableSeasonList.size() - 4; i > 0; i--) {
            if (removableSeasonList.get(i).startsWith(" ") || removableSeasonList.get(i).startsWith("<tr class=\"thead\"><td")) {
                removableSeasonList.remove(i);
            }
        }
        return removableSeasonList.toArray(new String[0]);
    }

    // helpers to find line numbers so the rest can be ignored
    private static int tbodyLocation (String[] htmlBlob) {
        for (int i = 0; i < htmlBlob.length; i++) {
            if (htmlBlob[i].contains("<tbody>")) {
                return i;
            }
        }
        return -1;
    }

    private static int tbodyEndLocation (String[] htmlBlob) {
        for (int i = 0; i < htmlBlob.length; i++) {
            if (htmlBlob[i].contains("</tbody>")) {
                return i;
            }
        }
        return -1;
    }

    private static int trhLocation (String[] htmlBlob) {
        for (int i = 0; i < htmlBlob.length; i++) {
            if (htmlBlob[i].startsWith("<tr ><th")) {
                return i;
            }
        }
        return -1;
    }

    private static int trhEndLocation (String[] htmlBlob) {
        for (int i = htmlBlob.length - 1; i > 0; i--) {
            if (htmlBlob[i].contains("</td></tr>")) {
                return i;
            }
        }
        return -1;
    }

    // reads a text file for its line
    private static List<String> readFile (String fileName) throws Exception {
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

    private static void addRows(TeamSeason returnee, String[] years, String[] colNames, String[] deletees) {
        String[] names = new String[years.length];
        double[][] table = new double[years.length][];
        for (int i = 0; i < years.length; i++) {
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
        returnee.addPlayers(colNames, names, table);
        returnee.deleteCols(deletees);
    }

    // Adds spaces to a row's columns to allow the later parsing by split to be easier once JSoup's parser removes junk.
    private static String addSpaces (String row) {
        char[] letters = row.toCharArray();
        int j = 0; // Position in row
        for (int i = 0; i < letters.length; i++, j++) {
            if (letters[i] == '<' && i > 0 && letters[i-1] != '>') {
                row = row.substring(0, j) + " " + row.substring(j);
                j += 1;
            }
        }
        return row;
    }

    // Gets an array of all attributes that the row has.
    // TODO: fix the fact undefined values don't get added. So don't remove empties? Then put blank
    // in the col names, and delete blank cols as many times as necessary afterwards
    private static String[] relevantChildren(String blob) {
        String[] lines = blob.split("\n");
        StringBuilder allLines = new StringBuilder();
        for (String line : lines) {
            allLines.append(line);
        }
        String[] children = allLines.toString().split("<.*?>");
        String name = children[1].substring(0,children[1].length()-1);
        StringBuilder allChildren = new StringBuilder();
        for (int i = 2; i <  children.length; i++) {
            allChildren.append(children[i]);
        }
        String[] temp = allChildren.toString().split(" +");
        String[] returnee = new String[temp.length + 1];
        System.arraycopy(temp,0,returnee,1,temp.length);
        returnee[0] = name;
        return returnee;
    }

    // Finds all the rows of a table with a certain identifier by seeking out its commented out version
    // and then finding only the important rows and splitting them into a list.
    private static String[] tableRows(Element container, String tableSelector) {
        Node comment = container.selectFirst(tableSelector);
        return tbodySeasons(comment.childNode(comment.childNodeSize()-2).outerHtml().split("[\\r\\n]+"));
    }
}
