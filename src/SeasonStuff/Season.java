package SeasonStuff;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

public class Season {
    public final String name;
    private Map<String, Double> tableVals;

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

    public String getCSV() {
        StringBuilder sb = new StringBuilder();
        for (Double value : tableVals.values()) {
            sb.append(value);
            sb.append(",");
        }
        return sb.toString();
    }

    public void addElem(String name, double val) {
        tableVals.put(name, val);
    }

    public double getElem(String name) {
        return tableVals.get(name);
    }
}
