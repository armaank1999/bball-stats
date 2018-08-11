import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Scraper {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            final Document document = Jsoup.connect("https://www.basketball-reference.com/players/b/bryanko01.html").get();
            Element statBlob = document.selectFirst("[role=main]");
            System.out.println(statBlob.outerHtml());
//            System.out.println(document.outerHtml());
            return;
        }
        String fileName = args[0];

    }
}
