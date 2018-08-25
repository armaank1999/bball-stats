package SeasonStuff;

import java.util.List;
import java.util.Iterator;
import java.lang.Iterable;

public class PlayerSeasonList implements Iterable<Season> {
    private final List<Season> seasons;
    public int zeroIndexedSeason;
    public int lastIndexedSeason;
    private boolean increasing;

    public PlayerSeasonList(List<Season> yrs) {
        seasons = yrs;
        zeroIndexedSeason = Integer.parseInt(yrs.get(0).name);
        increasing = Integer.parseInt(yrs.get(1).name) > zeroIndexedSeason;
        lastIndexedSeason = Integer.parseInt(yrs.get(yrs.size() - 1).name);
    }

    // Only works if no seasons missing - so a player who leaves the league like Rick Barry must be split
    public Season getSeason(int year) {
        if (increasing) return seasons.get(year - zeroIndexedSeason);
        return seasons.get(zeroIndexedSeason - year);
    }

    @Override
    public Iterator<Season> iterator() {
        return seasons.iterator();
    }
}
