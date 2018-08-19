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

    public static final String baseTeamUrl = "https://www.basketball-reference.com/teams/";

    public static void main(String[] args) throws Exception {
        readSeasonLink("DET", 2004);
    }

    private static void readSeasonLink(String team, int year) throws Exception {
        TeamSeason parsedInfo = new TeamSeason(year, team);
        String seasonLink = baseTeamUrl + team + "/" + year + ".html";
        Document statsDoc = Jsoup.connect(seasonLink).get();
        Element statBlob = statsDoc.selectFirst("[role=main]");
        Element advanced = statBlob.selectFirst("div#all_advanced");
        // Unfortunately, it's wrapped in a comment for some reason,
        // so ignore the comment parts with the next lines and retrieve the real table.
        Node advancedComment = advanced.childNode(advanced.childNodeSize()-2);
        String advancedCommentBlob = advancedComment.outerHtml();
        String[] splitRow = advancedCommentBlob.split("[\\r\\n]+");
        List<String> playerSeasons = tbodySeasons(splitRow);
        parseAdvanced(parsedInfo, playerSeasons);
        parsedInfo.printAllInfo();
        parsedInfo.saveFile();
    }

    // Get only the lines that are in the tbody of a comment blob.
    private static List<String> tbodySeasons(String[] allSeasons) {
        int lastIgnoredRow = tbodyLocation(allSeasons);
        int numPlayers = tbodyEndLocation(allSeasons) - (lastIgnoredRow + 1);
        String[] playerSeasons = new String[numPlayers];
        System.arraycopy(allSeasons,lastIgnoredRow + 1, playerSeasons, 0, numPlayers);
        return Arrays.asList(playerSeasons);
    }

    // helpers to find the line numbers that have tbody in a blob so the rest can be ignored
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

    private static void parseAdvanced (TeamSeason returnee, List<String> years) {
        String[] names = new String[years.size()];
        double[][] table = new double[years.size()][];
        for (int i = 0; i < years.size(); i++) {
            Element row = Jsoup.parse(addSpaces(years.get(i)));
            String[] colVals = nonEmptyChildren(row.outerHtml().split("<body>\n {2}")[1].split("\n </body>")[0]);
            String rowName = colVals[0];
            double[] colAsNums = new double[colVals.length - 1];
            for (int j = 0; j < colAsNums.length; j++) {
                colAsNums[j] = Double.parseDouble(colVals[j + 1]);
            }
            names[i] = rowName;
            table[i] = colAsNums;
        }
        returnee.addPlayers(advancedRows, names, table);
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
    private static String[] nonEmptyChildren(String blob) {
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
}
