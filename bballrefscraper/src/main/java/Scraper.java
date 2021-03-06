import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scraper {
    //<editor-fold desc="Massive list of constants">
    // List of valid teams by year.
    private static final String[][] teamNames = {
        //  0 - Present
        {"ATL", "BOS", "BRK", "CHO", "CHI", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MEM", "MIA",
            "MIL", "MIN", "NOP", "NYK", "OKC", "ORL", "PHI", "PHO", "POR", "SAC", "SAS", "TOR", "UTA", "WAS"},
        // 1,2,3 - 2012, 2013, 2014, nets to brooklyn, Hornets became pelicans, bobcats became hornets
        {"ATL", "BOS", "BRK", "CHA", "CHI", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MEM", "MIA",
            "MIL", "MIN", "NOP", "NYK", "OKC", "ORL", "PHI", "PHO", "POR", "SAC", "SAS", "TOR", "UTA", "WAS"},
        {"ATL", "BOS", "BRK", "CHA", "CHI", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MEM", "MIA",
            "MIL", "MIN", "NOH", "NYK", "OKC", "ORL", "PHI", "PHO", "POR", "SAC", "SAS", "TOR", "UTA", "WAS"},
        {"ATL", "BOS", "CHA", "CHI", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MEM", "MIA", "MIL",
            "MIN", "NJN", "NOH", "NYK", "OKC", "ORL", "PHI", "PHO", "POR", "SAC", "SAS", "TOR", "UTA", "WAS"},
        // 4 - 2008, Sonics to Thunder
        {"ATL", "BOS", "CHA", "CHI", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MEM", "MIA", "MIL",
            "MIN", "NJN", "NOH", "NYK", "ORL", "PHI", "PHO", "POR", "SAC", "SAS", "SEA", "TOR", "UTA", "WAS"},
        // 5,6 - 06, 07, hornets move to OKC and then back
        {"ATL", "BOS", "CHA", "CHI", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MEM", "MIA", "MIL",
            "MIN", "NJN", "NOK", "NYK", "ORL", "PHI", "PHO", "POR", "SAC", "SAS", "SEA", "TOR", "UTA", "WAS"},
        {"ATL", "BOS", "CHA", "CHI", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MEM", "MIA", "MIL",
            "MIN", "NJN", "NOH", "NYK", "ORL", "PHI", "PHO", "POR", "SAC", "SAS", "SEA", "TOR", "UTA", "WAS"},
        // 7, 8 - Bobcats added, Hornets to new orleans
        {"ATL", "BOS", "CHI", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MEM", "MIA", "MIL",
            "MIN", "NJN", "NOH", "NYK", "ORL", "PHI", "PHO", "POR", "SAC", "SAS", "SEA", "TOR", "UTA", "WAS"},
        {"ATL", "BOS", "CHI", "CHH", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MEM", "MIA",
            "MIL", "MIN", "NJN", "NYK", "ORL", "PHI", "PHO", "POR", "SAC", "SAS", "SEA", "TOR", "UTA", "WAS"},
        // 9,10  - Grizzlies to Memphis, Bullets to Wizards
        {"ATL", "BOS", "CHI", "CHH", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MIA", "MIL",
            "MIN", "NJN", "NYK", "ORL", "PHI", "PHO", "POR", "SAC", "SAS", "SEA", "TOR", "UTA", "VAN", "WAS"},
        {"ATL", "BOS", "CHI", "CHH", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MIA", "MIL",
            "MIN", "NJN", "NYK", "ORL", "PHI", "PHO", "POR", "SAC", "SAS", "SEA", "TOR", "UTA", "VAN", "WSB"},
        // 11,12,13 - Grizzlies, Raptors, Magic,Twolves, Hornets, Heat added
        {"ATL", "BOS", "CHI", "CHH", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MIA",
            "MIL", "MIN", "NJN", "NYK", "ORL", "PHI", "PHO", "POR", "SAC", "SAS", "SEA", "UTA", "WSB"},
        {"ATL", "BOS", "CHI", "CHH", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MIA",
            "MIL", "NJN", "NYK", "PHI", "PHO", "POR", "SAC", "SAS", "SEA", "UTA", "WSB"},
        {"ATL", "BOS", "CHI", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "LAC", "LAL", "MIL",
            "NJN", "NYK", "PHI", "PHO", "POR", "SAC", "SAS", "SEA", "UTA", "WSB"},
        // 14, 15 - Kings to sacramento, clippers to LA
        {"ATL", "BOS", "CHI", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "KCK", "LAC", "LAL", "MIL",
            "NJN", "NYK", "PHI", "PHO", "POR", "SAS", "SEA", "UTA", "WSB"},
        {"ATL", "BOS", "CHI", "CLE", "DAL", "DEN", "DET", "GSW", "HOU", "IND", "KCK", "LAL", "MIL",
            "NJN", "NYK", "PHI", "PHO", "POR", "SAS", "SDC", "SEA", "UTA", "WSB"},
        // 16 - Mavericks added
        {"ATL", "BOS", "CHI", "CLE", "DEN", "DET", "GSW", "HOU", "IND", "KCK", "LAL",
            "MIL", "NJN", "NYK", "PHI", "PHO", "POR", "SAS", "SDC", "SEA", "UTA", "WSB"},
        // 17 - Jazz to Utah, Clippers to san diego, nets to new jersey
        {"ATL", "BOS", "CHI", "CLE", "DEN", "DET", "GSW", "HOU", "IND", "KCK", "LAL",
            "MIL", "NJN", "NOJ", "NYK", "PHI", "PHO", "POR", "SAS", "SDC", "SEA", "WSB"},
        {"ATL", "BOS", "BUF", "CHI", "CLE", "DEN", "DET", "GSW", "HOU", "IND", "KCK",
            "LAL", "MIL", "NJN", "NOJ", "NYK", "PHI", "PHO", "POR", "SAS", "SEA", "WSB"},
        {"ATL", "BOS", "BUF", "CHI", "CLE", "DEN", "DET", "GSW", "HOU", "IND", "KCK",
            "LAL", "MIL", "NOJ", "NYK", "NYN", "PHI", "PHO", "POR", "SAS", "SEA", "WSB"},
        // 20 - ABA Merger
        {"ATL", "BOS", "BUF", "CHI", "CLE", "DET", "GSW", "HOU", "KCK", "LAL", "MIL", "NOJ", "NYK", "PHI", "PHO", "POR", "SEA", "WSB"},
        // 21-25 - Kings moved; Jazz added, Bullets name change; bullets name change; kings moved; warriors name change, rockets move
        {"ATL", "BOS", "BUF", "CHI", "CLE", "DET", "GSW", "HOU", "KCO", "LAL", "MIL", "NOJ", "NYK", "PHI", "PHO", "POR", "SEA", "WSB"},
        {"ATL", "BOS", "BUF", "CAP", "CHI", "CLE", "DET", "GSW", "HOU", "KCO", "LAL", "MIL", "NYK", "PHI", "PHO", "POR", "SEA"},
        {"ATL", "BAL", "BOS", "BUF", "CHI", "CLE", "DET", "GSW", "HOU", "KCO", "LAL", "MIL", "NYK", "PHI", "PHO", "POR", "SEA"},
        {"ATL", "BAL", "BOS", "BUF", "CHI", "CIN", "CLE", "DET", "GSW", "HOU", "LAL", "MIL", "NYK", "PHI", "PHO", "POR", "SEA"},
        {"ATL", "BAL", "BOS", "BUF", "CHI", "CIN", "CLE", "DET", "LAL", "MIL", "NYK", "PHI", "PHO", "POR", "SDR", "SEA", "SFW"},
        // 26-29 - Braves, cavs, blazers added; Bucks and suns added; sonics and rockets added, hawks moved; bulls added
        {"ATL", "BAL", "BOS", "CHI", "CIN", "DET", "LAL", "MIL", "NYK", "PHI", "PHO", "SDR", "SEA", "SFW"},
        {"BAL", "BOS", "CHI", "CIN", "DET", "LAL", "NYK", "PHI", "SDR", "SEA", "SFW", "STL"},
        {"BAL", "BOS", "CHI", "CIN", "DET", "LAL", "NYK", "PHI", "SFW", "STL"},
        {"BAL", "BOS", "CIN", "DET", "LAL", "NYK", "PHI", "SFW", "STL"},
        // 30-33 - Nats to sixers, bullets form, warriors move, lakers move, royals + pistons move, hawks move
        {"BOS", "CHZ", "CIN", "DET", "LAL", "NYK", "SFW", "STL", "SYR"},
        {"BOS", "CHP", "CIN", "DET", "LAL", "NYK", "PHW", "STL", "SYR"},
        {"BOS", "CIN", "DET", "LAL", "NYK", "PHW", "STL", "SYR"},
        {"BOS", "CIN", "DET", "MNL", "NYK", "PHW", "STL", "SYR"},
        {"BOS", "FTW", "MNL", "NYK", "PHW", "ROC", "STL", "SYR"},
        {"BOS", "FTW", "MLH", "MNL", "NYK", "PHW", "ROC", "SYR"}};
    // First is 1980 to present, No 3PAr in 78 and 79, No TOV or USG from 74-77, No O/DRB, STL/BLK, BPM from 71 to 73,
    // No TRB 65 - 70, 1952 - 1964 no AST. Pre 1952 doesn't even have minutes played so not bothering
    private static final String[][] advancedCols = {
        {"Age", "G", "MP", "PER", "TS%", "3PAr", "FTr", "ORB%", "DRB%", "TRB%", "AST%", "STL%",
            "BLK%", "TOV%", "USG%", "OWS", "DWS", "WS", "WS/48", "OBPM", "DBPM", "BPM", "VORP"},
        {"Age", "G", "MP", "PER", "TS%", "FTr", "ORB%", "DRB%", "TRB%", "AST%", "STL%",
            "BLK%", "TOV%", "USG%", "OWS", "DWS", "WS", "WS/48", "OBPM", "DBPM", "BPM", "VORP"},
        {"Age", "G", "MP", "PER", "TS%", "FTr", "ORB%", "DRB%", "TRB%", "AST%", "STL%",
            "BLK%", "OWS", "DWS", "WS", "WS/48", "OBPM", "DBPM", "BPM", "VORP"},
        {"Age", "G", "MP", "PER", "TS%", "FTr", "TRB%", "AST%", "OWS", "DWS", "WS", "WS/48"},
        {"Age", "G", "MP", "PER", "TS%", "FTr", "AST%", "OWS", "DWS", "WS", "WS/48"},
        {"Age", "G", "MP", "PER", "TS%", "FTr", "OWS", "DWS", "WS", "WS/48"}};
    // First 1982 to present, no GS 80 and 81, no 3P 78 and 79, no TOV or ORTG 74 to 77, nothing before then.
    private static final String[][] per100Cols = {
        {"Age", "G", "GS", "MP", "FG", "FGA", "FG%", "3P", "3PA", "3P%", "2P", "2PA", "2P%", "FT",
            "FTA", "FT%", "ORB", "DRB", "TRB", "AST", "STL", "BLK", "TOV", "PF", "PTS", "ORtg", "DRtg"},
        {"Age", "G", "MP", "FG", "FGA", "FG%", "3P", "3PA", "3P%", "2P", "2PA", "2P%", "FT",
            "FTA", "FT%", "ORB", "DRB", "TRB", "AST", "STL", "BLK", "TOV", "PF", "PTS", "ORtg", "DRtg"},
        {"Age", "G", "MP", "FG", "FGA", "FG%", "2P", "2PA", "2P%", "FT", "FTA", "FT%",
            "ORB", "DRB", "TRB", "AST", "STL", "BLK", "TOV", "PF", "PTS", "ORtg", "DRtg"},
        {"Age", "G", "MP", "FG", "FGA", "FG%", "2P", "2PA", "2P%", "FT", "FTA", "FT%",
            "ORB", "DRB", "TRB", "AST", "STL", "BLK", "PF", "PTS", "DRtg"}};
    // The same regardless of the year; only apply for 2001 onwards.
    private static final String[] onOffCols = {"%MP", "OoTmEFG%", "OoORB%", "OoDRB%", "OoTRB%", "OoTmAST%", "OoTmSTL%",
        "OoTmBLK%", "OoTmTOV%", "OoTmPace", "OoORtg", "OoOpEFG%", "OoOpORB%", "OoOpDRB%", "OoOpTRB%", "OoOpAST%", "OoOpSTL%",
        "OoOpBLK%", "OoOpTOV%", "OoOpPace", "OoDRtg", "OoNtEFG%", "OoNtORB%", "OoNtDRB%", "OoNtTRB%", "OoNtAST%", "OoNtSTL%",
        "OoNtBLK%", "OoNtTOV%", "OoNtPace", "OoNtRtg"};
    // First since 1980, second since 1974 - no threes - then miss ORB, then miss opponent info pre 71. We ignore Arena and Attendance.
    private static final String[][] topTeamCols = {
        {"MP", "FG", "FGA", "FG%", "3P", "3PA", "3P%", "2P", "2PA", "2P%", "FT", "FTA", "FT%", "ORB", "DRB", "TRB",
            "AST", "STL", "BLK", "TOV"},
        {"MP", "FG", "FGA", "FG%", "FT", "FTA", "FT%", "ORB", "DRB", "TRB", "AST", "STL", "BLK", "TOV"},
        {"MP", "FG", "FGA", "FG%", "FT", "FTA", "FT%", "TRB", "AST"},
        {"FG", "FGA", "FG%", "FT", "FTA", "FT%", "TRB", "AST"}};
    // First since 1980, second since 1977 - no threes, then no OvsDrb, then no opp info.
    private static final String[][] bottomTeamCols = {
        {"SRS", "ORtg", "DRtg", "Pace", "FTr", "3PAr", "eFG%", "TOV%", "ORB%", "FT/FG", "OppeFG%", "OppTOV%", "DRB%", "OppFT/FG"},
        {"SRS", "ORtg", "DRtg", "Pace", "FTr", "eFG%", "TOV%", "ORB%", "FT/FG", "OppeFG%", "OppTOV%", "DRB%", "OppFT/FG"},
        {"SRS", "ORtg", "DRtg", "Pace", "FTr", "eFG%", "FT/FG", "OppeFG%", "OppFT/FG"},
        {"SRS", "ORtg", "DRtg", "Pace", "FTr", "eFG%", "FT/FG"}};

    // Cols from each table that are not needed. Per 100 also has duplicate cols with advanced, so those are included.
    private static final String[] advancedIgnorees = {"VORP", "DBPM", "OBPM", "DWS", "OWS", "TRB%", "PER"};
    private static final String[] per100Ignorees = {"PF", "TRB", "ORB", "DRB", "MP", "GS", "G", "Age"};
    private static final String[] onOffIgnorees = {"OoNtRtg", "OoNtPace", "OoNtTOV%", "OoNtEFG%", "OoNtBLK%", "OoNtSTL%",
        "OoNtAST%", "OoNtTRB%", "OoNtDRB%", "OoNtORB%", "OoNtEFG%", "OoOpBLK%", "OoOpSTL%", "OoOpAST%", "OoOpTRB%",
        "OoOpDRB%", "OoOpORB%", "OoOpPace", "OoTRB%", "OoTmBLK%", "OoTmSTL%", "OoTmAST%", "OoTmPace"};
    private static final String[] topTeamTableIgnorees = {"STL", "DRB", "ORB"};
    private static final String[] bottomTeamTableIgnorees = {"FTr"};
    // TODO: Scrap FT/FGA, calculate opp FTr, and assign these to oppCols.
    //</editor-fold>

    // Want the averages of allYears to be a global variable, and it is static as there's only one overarching average file.
    private static SeasonList allYears;

    public static void main(String[] args) throws Exception {
        allYears = SeasonList.readSeasonList("allyears.csv");
        parseSeasons( 2019);
    }

    private static void parseSeasons(int year) throws Exception {
        String[] teams = getTeams(year);
        if (teams == null) return;
        for (String team : teams) parseSeason(team, year);
    }

    private static void parseSeason(String team, int year) throws Exception {
        if (year > 2019) return;
        TeamSeason parsedInfo = new TeamSeason(team, year);
        readSeasonLink(parsedInfo);
        if (year > 2000) readOnOffLink(parsedInfo);
        else parsedInfo.addMinAdj();
    }

    // Functions that read the relevant link with JSoup, then find the right info, parse it, and add to the TeamSeason.
    private static void readOnOffLink(TeamSeason szn) throws Exception {
        // First find the url and table we want. Find the real content of the table with our helper.
        // Then call our add rows helper to add the rows and remove the ones we don't want.
        Document statsDoc = Jsoup.connect(szn.url("/on-off")).get();
        String[] splitComment = parseComment(statsDoc.selectFirst("[role=main]").selectFirst("div#all_on_off"));
        int lastIgnoredRow = searchFromFront("<tr ><th", splitComment);
        int numPlayers = searchFromEnd("</td></tr>", splitComment) - lastIgnoredRow + 1;
        String[] playerSeasons = new String[numPlayers];
        System.arraycopy(splitComment, lastIgnoredRow, playerSeasons, 0, numPlayers);
        // Now remove all the additional headers that are in between actual rows. Don't need to check last three
        List<String> returnee = new ArrayList<>(Arrays.asList(playerSeasons));
        for (int i = returnee.size() - 4; i > 0; i--)
            if (returnee.get(i).startsWith(" ") || returnee.get(i).startsWith("<tr class=\"thead\"><td"))
                returnee.remove(i);
        addOnOffRows(szn, returnee.toArray(new String[0]));
        szn.deleteCols(onOffIgnorees);
        szn.addAdjustments();
    }

    private static void readSeasonLink(TeamSeason szn) throws Exception {
        // First find the url and table we want. Find the real content of the table(s) with our helper.
        // Then call our add rows helper to add the rows and remove the ones we don't want.
        String[] advancedNames = getAdvancedCols(szn.year);
        if (advancedNames == null) return;
        Document statsDoc = Jsoup.connect(szn.url(".html")).get();
//        Element blob = statsDoc.selectFirst("[role=main]");
//        addTeamInfo(szn, parseComment(blob.selectFirst("div#all_team_and_opponent")), parseComment(blob.selectFirst("div#all_team_misc")));
//        addRows(szn, tableRows(blob, "div#all_advanced"), advancedNames);
//        szn.deleteCols(advancedIgnorees);
//        String[] hundredNames = getPer100Cols(szn.year);
//        if (hundredNames != null) {
//            addRows(szn, tableRows(blob, "div#all_per_poss"), hundredNames);
//            szn.deleteCols(per100Ignorees);
//        }
    }

    // helpers to find line numbers so the rest can be ignored, and then the relevant rows are parsed
    private static int searchFromFront(String pattern, String[] rows) {
        for (int i = 0; i < rows.length; i++)
            if (rows[i].contains(pattern))
                return i;
        return -1;
    }

    private static int searchFromEnd(String pattern, String[] rows) {
        for (int i = rows.length - 1; i > 0; i--)
            if (rows[i].contains(pattern))
                return i;
        return -1;
    }

    // functions to take the rows, parse them properly, and then add their info to the szn
    private static void addRows(TeamSeason szn, String[] years, String[] colNames) {
        String[] names = new String[years.length];
        double[][] table = new double[years.length][];
        for (int i = 0; i < years.length; i++) {
            Element row = Jsoup.parse(addSpaces(years[i]));
            String[] colVals = relevantChildren(row.outerHtml().split("<body>\n  ")[1].split("\n </body>")[0]);
            String name = colVals[0];
            double[] colAsNums = new double[colVals.length - 1];
            for (int j = 0; j < colAsNums.length; j++)
                colAsNums[j] = Double.parseDouble(colVals[j + 1]);
            names[i] = name;
            table[i] = colAsNums;
        }
        szn.addPlayerStats(colNames, names, table);
    }

    private static void addOnOffRows(TeamSeason szn, String[] rows) {
        // Each player row is 3 high - on, off, difference. We only want the difference for now, except for name which is in 1.
        String[] names = new String[rows.length / 3];
        double[][] colVals = new double[rows.length / 3][];
        for (int i = 0; i < rows.length; i += 3) {
            // Assign the name, which is in the hyperlink tag (so in the tag after the initial link, which ends in .html)
            names[i / 3] = rows[i].split(".*html\">")[1].split("<")[0];
            List<String> allSplitRows = new ArrayList<>(Arrays.asList(rows[i + 2].split("<.*?>")));
            // Remove the blank columns that are in between the meaningful ones, and the first which is a pointless text tag.
            for (int j = allSplitRows.size() - 1; j >= 0; j--)
                if (allSplitRows.get(j).equals("")) allSplitRows.remove(j);
            allSplitRows.remove(0);
            String[] realValues = allSplitRows.toArray(new String[0]);
            double[] trulyParsedValues = new double[realValues.length];
            // the first one is represented as a percentage so ignore the percentage sign
            trulyParsedValues[0] = Double.parseDouble(realValues[0].substring(0, realValues[0].length() - 1));
            for (int k = 1; k < realValues.length; k++)
                trulyParsedValues[k] = Double.parseDouble(realValues[k]);
            colVals[i / 3] = trulyParsedValues;
        }
        szn.addPlayerStats(onOffCols, names, colVals);
    }

    // Adds spaces to a row's columns to allow the later parsing by split to be easier once JSoup's parser removes junk.
    private static String addSpaces(String row) {
        char[] letters = row.toCharArray();
        int j = 0; // Position in row
        for (int i = 0; i < letters.length; i++, j++)
            if (letters[i] == '<' && i > 0 && letters[i - 1] != '>') {
                row = row.substring(0, j) + " " + row.substring(j);
                j += 1;
            }
        return row;
    }

    // Gets an array of all attributes that the row has. First is rank, which we ignore. Second is name, which is
    // wrapped in a href so it's extracted differently from the others
    private static String[] relevantChildren(String blob) {
        String[] children = String.join("", blob.split("\n")).split("<.*?>");
        String name = children[1].substring(0, children[1].length() - 1);
        StringBuilder allChildren = new StringBuilder();
        for (int i = 2; i < children.length; i++)
            allChildren.append(children[i]);
        String[] temp = allChildren.toString().split(" +");
        String[] returnee = new String[temp.length + 1];
        System.arraycopy(temp, 0, returnee, 1, temp.length);
        returnee[0] = name;
        return returnee;
    }

    // Finds all the rows of a table with a certain identifier by seeking out its commented out version
    // and then finding only the important rows and splitting them into an array.
    private static String[] tableRows(Element container, String tableSelector) {
        String[] splitComment = parseComment(container.selectFirst(tableSelector));
        int firstRow = searchFromFront("<tbody>", splitComment) + 1;
        int numPlayers = searchFromEnd("</tbody>", splitComment) - firstRow;
        String[] playerSeasons = new String[numPlayers];
        System.arraycopy(splitComment, firstRow, playerSeasons, 0, numPlayers);
        return playerSeasons;
    }

    // Takes the selected top and bottom rows from their comment blobs, parses them, and adds it to the team season.
    private static void addTeamInfo(TeamSeason szn, String[] topRows, String[] bottomRows) {
        String[] topLabels = getTopTeamCols(szn.year);
        if (topLabels == null) return;

        String topTeamVal = topRows[searchFromEnd("Team/G", topRows)];
        String[] topSplitArr = String.join("  ", topTeamVal.split("<.*?>")).split("  +");
        double[] topRelevantArr = new double[topSplitArr.length - 4];
        for (int i = 0; i < topRelevantArr.length; i++)
            topRelevantArr[i] = Double.parseDouble(topSplitArr[i+2]);

        szn.addTeamAttributes(topLabels, topRelevantArr);
        szn.deleteTeamCols(topTeamTableIgnorees);

        if (szn.year > 1970) {
            String topOppVal = topRows[searchFromEnd("Opponent/G", topRows)];
            String[] oppSplitArr = String.join("  ", topOppVal.split("<.*?>")).split("  +");
            double[] oppRelevantArr = new double[oppSplitArr.length - 4];
            for (int i = 0; i < oppRelevantArr.length; i++)
                oppRelevantArr[i] = Double.parseDouble(oppSplitArr[i + 2]);
            szn.addOppAttributes(topLabels, oppRelevantArr);
            szn.deleteOppCols(topTeamTableIgnorees);
        }

        String[] bottomLabels = getBottomTeamCols(szn.year);
        String bottomTeamVal = bottomRows[searchFromFront("<tr >", bottomRows)];
        String[] bottomSplitArr = String.join("  ", bottomTeamVal.split("<.*?>")).split("  +");
        double[] bottomRelevantArr = new double[bottomLabels.length];
        for (int i = 0; i < bottomRelevantArr.length; i++)
            bottomRelevantArr[i] = Double.parseDouble(bottomSplitArr[i+8]);
        szn.addTeamAttributes(bottomLabels, bottomRelevantArr);
        szn.deleteTeamCols(bottomTeamTableIgnorees);

        szn.per100ize();
        szn.addRelativeInfo(allYears);
    }

    // Takes the node that has the commented out table and returns the table, split into lines.
    private static String[] parseComment(Node blob) {
        return blob.childNode(blob.childNodeSize() - 2).outerHtml().split("[\\r\\n]+");
    }

    //<editor-fold desc="Get the columns for the given year.">
    private static String[] getPer100Cols(int year) {
        if (year > 1981) return per100Cols[0];
        if (year > 1979) return per100Cols[1];
        if (year > 1977) return per100Cols[2];
        if (year > 1973) return per100Cols[3];
        return null;
    }

    private static String[] getAdvancedCols(int year) {
        if (year > 1979) return advancedCols[0];
        if (year > 1977) return advancedCols[1];
        if (year > 1973) return advancedCols[2];
        if (year > 1970) return advancedCols[3];
        if (year > 1964) return advancedCols[4];
        if (year > 1951) return advancedCols[5];
        return null;
    }

    private static String[] getTopTeamCols(int year) {
        if (year > 1979) return topTeamCols[0];
        if (year > 1973) return topTeamCols[1];
        if (year > 1964) return topTeamCols[2];
        if (year > 1950) return topTeamCols[3];
        return null;
    }

    private static String[] getBottomTeamCols(int year) {
        if (year > 1979) return bottomTeamCols[0];
        if (year > 1973) return bottomTeamCols[1];
        if (year > 1970) return bottomTeamCols[2];
        if (year > 1950) return bottomTeamCols[3];
        return null;
    }

    private static String[] getTeams(int year) {
        if (year > 2019) return null;
        if (year > 2014) return teamNames[0];
        if (year > 2013) return teamNames[1];
        if (year > 2012) return teamNames[2];
        if (year > 2008) return teamNames[3];
        if (year > 2007) return teamNames[4];
        if (year > 2005) return teamNames[5];
        if (year > 2004) return teamNames[6];
        if (year > 2002) return teamNames[7];
        if (year > 2001) return teamNames[8];
        if (year > 1997) return teamNames[9];
        if (year > 1995) return teamNames[10];
        if (year > 1989) return teamNames[11];
        if (year > 1988) return teamNames[12];
        if (year > 1985) return teamNames[13];
        if (year > 1984) return teamNames[14];
        if (year > 1980) return teamNames[15];
        if (year > 1979) return teamNames[16];
        if (year > 1978) return teamNames[17];
        if (year > 1977) return teamNames[18];
        if (year > 1976) return teamNames[19];
        if (year > 1975) return teamNames[20];
        if (year > 1974) return teamNames[21];
        if (year > 1973) return teamNames[22];
        if (year > 1972) return teamNames[23];
        if (year > 1971) return teamNames[24];
        if (year > 1970) return teamNames[25];
        if (year > 1968) return teamNames[26];
        if (year > 1967) return teamNames[27];
        if (year > 1966) return teamNames[28];
        if (year > 1963) return teamNames[29];
        if (year > 1962) return teamNames[30];
        if (year > 1961) return teamNames[31];
        if (year > 1960) return teamNames[32];
        if (year > 1957) return teamNames[33];
        if (year > 1955) return teamNames[34];
        if (year > 1951) return teamNames[35];
        return null;
    }
    //</editor-fold>
}
