import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Arrays;

public class TeamSeason {
    private Map<String, List> playerSeasons;
    private List<String> colNames;
    private int year;
    private String team;

    public TeamSeason(int y, String t) {
        year = y;
        team = t;
        playerSeasons = new LinkedHashMap<String, List>();
        colNames = new ArrayList<String>();
    }

    public void addPlayers(String[] newColNames, String[] names, double[][] colVals) {
        colNames.addAll(Arrays.asList(newColNames));
    }

    public void printAllInfo() {
        System.out.println("Parsed from " + url());
    }
    
    public String url() {
        return Scraper.baseTeamUrl + team + "/" + year + ".html";
    }

}
