package SeasonStuff;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.lang.Iterable;

public class SeasonList implements Serializable, Iterable<Season> {
    private static final long serialVersionUID = 945228L;
    private final List<Season> seasons;
    public int zeroIndexedSeason;
    public int lastIndexedSeason;
    private boolean increasing;

    public SeasonList(List<Season> yrs) {
        seasons = yrs;
        zeroIndexedSeason = Integer.parseInt(yrs.get(0).name);
        increasing = Integer.parseInt(yrs.get(1).name) > zeroIndexedSeason;
        lastIndexedSeason = Integer.parseInt(yrs.get(yrs.size() - 1).name);
    }

    public void printManySeasons() {
        seasons.get(0).printNames();
        for (Season curr : seasons) {
            curr.printManySeasons();
        }
    }

    public void saveSeasonList(String name) {
        File f = new File("./" + name + ".txt");
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public static SeasonList loadSeasonList(String name) {
        File f = new File("./" + name + ".txt");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                return (SeasonList) os.readObject();
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
            } catch (IOException e) {
                System.out.println(e.toString());
            } catch (ClassNotFoundException e) {
                System.out.println("Class error");
            }
        }
        return null;
    }

    // Only works if no seasons missing - so a player who leaves the league like Rick Barry must be split
    public Season getSeason(int year) {
        if (increasing) return seasons.get(year-zeroIndexedSeason);
        return seasons.get(zeroIndexedSeason-year);
    }

    @Override
    public Iterator<Season> iterator() {
        return seasons.iterator();
    }
}
