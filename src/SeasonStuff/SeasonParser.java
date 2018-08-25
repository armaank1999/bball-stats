package SeasonStuff;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SeasonParser {
    private static final String CSV_SPLIT_BY = ",";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No file name(s) specified");
            return;
        }
        String fileName = args[0];
        PlayerSeasonList curr = readFile(fileName);
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                fileName = args[i];
                PlayerSeasonList next = readFile(fileName);
                for (Season currentYear : next) {
                    currentYear.addElem("SPA", SPA(currentYear,
                            curr.getSeason((int) currentYear.getElem("Yr"))));
                }
            }
        }
    }

    /* Add Points based on the player's usage rate and scoring efficiency
     .08 term gives extra points for having extra usage to not overglorify
     low usage players, netEff term credits players for being efficient.
     Then add points for providing floor spacing. The /20 is subject to change
     One could theoretically add a FTr term but there are upsides (stop transition
     scoring, foul trouble) and downsides (less offensive boards) as well as momentum.
     .08 is also changeable but the WS formula uses .92 so I use this for now.*/
    private static double SPA(Season pS, Season yA) {
        double netEff = pS.getElem("TS%") - yA.getElem("TS%");
        double usage = pS.getElem("USG%");
        double net3PR = pS.getElem("3PAr") - yA.getElem("3PAr");
        return Math.floor((usage * (netEff + net3PR / 20.0) + .08 * (usage - 20.0)) * 1000) / 1000;
    }

    // Add Points based on the player's turnovers and assists
    private static double ATPA(Season pS, Season yA) {
        return 0;
    }

    private static PlayerSeasonList readFile(String fileName) {
        String line;
        String[] rowNames, currVals;
        List<Season> parsed = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            // Parse 1st line to get col names and # cols
            // 1st val must always be name/year so hash sets work properly
            rowNames = br.readLine().split(CSV_SPLIT_BY);
            while ((line = br.readLine()) != null) {
                currVals = line.split(CSV_SPLIT_BY);
                parsed.add(new Season(rowNames, currVals));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new PlayerSeasonList(parsed);
    }
}
