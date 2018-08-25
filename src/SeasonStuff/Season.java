package SeasonStuff;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

public class Season {
    private Map<String, Double> tableVals;
    public final String name;

    public Season(String n) {
        tableVals = new HashMap<>();
        name = n;
    }

    public Season(String[] names, String[] vals) {
        tableVals = new LinkedHashMap<>();
        name = vals[0];
        int len = names.length;
        for (int i = 0; i < len; i++) {
            tableVals.put(names[i], Double.parseDouble(vals[i]));
        }
    }

    public void printSingleSeason() {
        for (String key : tableVals.keySet()) {
            System.out.println(key + ": " + tableVals.get(key));
        }
    }

    public void printManySeasons() {
        for (Double value : tableVals.values()) {
            System.out.printf("%-7s", value);
        }
        System.out.println();
    }

    public void printNames() {
        for (String key : tableVals.keySet()) {
            System.out.printf("%-7s", key);
        }
        System.out.println();
    }

    public void addElem(String name, double val) {
        tableVals.put(name, val);
    }

    public double getElem(String name) {
        return tableVals.get(name);
    }
}
