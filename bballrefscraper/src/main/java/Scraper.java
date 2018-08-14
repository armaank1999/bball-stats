import java.util.Arrays;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Node;

public class Scraper {
    public static final String[] advancedRows = {"Blank", "Name", "Age", "G", "MP", "PER", "TS%", "3PAr", "FTr",
        "ORB%", "DRB%", "TRB%", "AST%", "STL%", "BLK%", "TOV%", "USG%", "Blank", "OWS", "DWS", "WS", "WS/48",
        "Blank", "OBPM", "DBPM", "BPM", "VORP"};

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            readSeasonLink("https://www.basketball-reference.com/teams/DEN/2001");
            return;
        }
        String fileName = args[0];

    }

    private static void readPlayerLink(String url) throws Exception {
        final Document document = Jsoup.connect(url).get();
        Element statBlob = document.selectFirst("[role=main]");
        Element advancedTable = statBlob.selectFirst("div#all_advanced");
        Node comment = advancedTable.childNode(advancedTable.childNodeSize()-2);
        System.out.println(comment.outerHtml());
        // Unfortunately, it's wrapped in a comment for some reason.
    }

    private static void readSeasonLink(String url) throws Exception {
        String seasonLink = url + ".html";
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
        int firstIgnoredRow = tbodyEndLocation(splitRow);
        String[] playerSeasons = Arrays.copyOfRange(splitRow,lastIgnoredRow+1,firstIgnoredRow);
        System.out.println(playerSeasons[0]);
        System.out.println(playerSeasons[playerSeasons.length-1]);

//        String onOffLink = url + "/on-off/";
//        final Document onOffDoc = Jsoup.connect(onOffLink).get();
//        Element onOffWrapper = onOffDoc.selectFirst("div#all_on_off");
//        Node onOffComment = onOffWrapper.childNode(advancedTable.childNodeSize()-2);
//        System.out.println(onOffComment.outerHtml());
        // Unfortunately, it's wrapped in a comment for some reason,
        // so ignore the comment parts with the next lines and retrieve the real table.
    }

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
}
