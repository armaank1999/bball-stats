import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Scraper {
    private static final String[] advancedRows = {"Age", "G", "MP", "PER", "TS%", "3PAr", "FTr",
        "ORB%", "DRB%", "TRB%", "AST%", "STL%", "BLK%", "TOV%", "USG%", "OWS", "DWS", "WS", "WS/48",
        "OBPM", "DBPM", "BPM", "VORP"};

    public static final String baseTeamUrl = "https://www.basketball-reference.com/teams/";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            readSeasonLink("DEN", 2001);
            return;
        }
        String fileName = args[0];
        List<String> years = readFile(fileName);
        TeamSeason mySeason = parseAdvanced("DEN", 2001, years);
    }

    private static void readPlayerLink(String url) throws Exception {
        final Document document = Jsoup.connect(url).get();
        Element statBlob = document.selectFirst("[role=main]");
        Element advancedTable = statBlob.selectFirst("div#all_advanced");
        Node comment = advancedTable.childNode(advancedTable.childNodeSize()-2);
        System.out.println(comment.outerHtml());
        // Unfortunately, it's wrapped in a comment for some reason.
    }

    private static void readSeasonLink(String team, int year) throws Exception {
        String seasonLink = baseTeamUrl + team + "/" + year + ".html";
        final Document statsDoc = Jsoup.connect(seasonLink).get();
        Element statBlob = statsDoc.selectFirst("[role=main]");
        Element advanced = statBlob.selectFirst("div#all_advanced");
        // Unfortunately, it's wrapped in a comment for some reason,
        // so ignore the comment parts with the next lines and retrieve the real table.
        Node advancedComment = advanced.childNode(advanced.childNodeSize()-2);
        String advancedCommentBlob = advancedComment.outerHtml();
        String[] splitRow = advancedCommentBlob.split("[\\r\\n]+");
        // Extract the tr with all the necessary info using split, table col names are always the same
        // so no need to extract them. Then put name, age, ..., in a TeamSeason object.
        // then add - +-, add that info to the TeamSeason object.
        // get the <tbody> from the split row, then each row after that needs to be parsed
        int lastIgnoredRow = tbodyLocation(splitRow);
        int numPlayers = tbodyEndLocation(splitRow) - (lastIgnoredRow + 1);
        String[] playerSeasons = new String[numPlayers];
        System.arraycopy(splitRow,lastIgnoredRow + 1, playerSeasons, 0, numPlayers);

//        String onOffLink = url + "/on-off/";
//        final Document onOffDoc = Jsoup.connect(onOffLink).get();
//        Element onOffWrapper = onOffDoc.selectFirst("div#all_on_off");
//        Node onOffComment = onOffWrapper.childNode(advancedTable.childNodeSize()-2);
//        System.out.println(onOffComment.outerHtml());
        // Unfortunately, it's wrapped in a comment for some reason,
        // so ignore the comment parts with the next lines and retrieve the real table.
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

    private static TeamSeason parseAdvanced (String team, int year, List<String> years) {
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
        TeamSeason returnee = new TeamSeason(year, team);
        returnee.addPlayers(advancedRows, names, table);
        return returnee;
    }

    // Adds spaces to a row's columns to allow the later parsing by split to be easier once JSoup's parser removes the junk.
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
        String allLines = "";
        for (String line : lines) {
            allLines += line;
        }
        String[] children = allLines.split("<.*?>");
        String name = children[1].substring(0,children[1].length()-1);
        String allChildren = "";
        for (int i = 2; i <  children.length; i++) {
            allChildren += children[i];
        }
        String[] temp = allChildren.split(" +");
        String[] returnee = new String[temp.length + 1];
        System.arraycopy(temp,0,returnee,1,temp.length);
        returnee[0] = name;
        return returnee;
    }
}
