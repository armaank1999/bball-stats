import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Node;

public class Scraper {

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
        advancedCommentBlob = advancedCommentBlob.substring(10,advancedCommentBlob.length()-5);
        Element advancedTable = new Element(advancedCommentBlob);
        System.out.println(advancedTable.outerHtml());

//        String onOffLink = url + "/on-off/";
//        final Document onOffDoc = Jsoup.connect(onOffLink).get();
//        Element onOffWrapper = onOffDoc.selectFirst("div#all_on_off");
//        Node onOffComment = onOffWrapper.childNode(advancedTable.childNodeSize()-2);
//        System.out.println(onOffComment.outerHtml());
        // Unfortunately, it's wrapped in a comment for some reason,
        // so ignore the comment parts with the next lines and retrieve the real table.
    }
}
