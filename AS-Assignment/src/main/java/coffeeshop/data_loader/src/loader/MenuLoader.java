package loader;
import model.MenuItem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
AI usage instructions:
Ask AI about the format of the Map container.
How to use BufferedReader to read files.
The usage of reading each line's function and the usage of processing each line's function for handling whitespace characters.
 */


public class MenuLoader {
     // CSV format : id, description, cost, category.If there are any illegal lines, such as incomplete data, ignore them
     
    public Map<String, MenuItem> load(String filename) {
        Map<String, MenuItem> result = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            for (String raw = reader.readLine(); raw != null; raw = reader.readLine()) {
                MenuItem parsed = parseLine(raw);
                if (parsed != null) {
                    result.put(parsed.getId(), parsed);
                }
            }

        } catch (IOException io) {

            System.out.println("Error!cannt read the file.  Name:" + filename);
        }

        return result;
    }


    //read menu,return:MenuItem Object
    private MenuItem parseLine(String rawLine) {
        if (rawLine == null) return null;

        String s = rawLine.trim();
        if (s.isEmpty()) return null;

        String[] fields = s.split(",", -1);
        if (fields.length < 4) {
            System.out.println("[MenuLoader] Skipped malformed line: " + rawLine);
            return null;
        }
        try {
            String id = fields[0].trim();
            String description = fields[1].trim();
            double cost = Double.parseDouble(fields[2].trim());
            String category = fields[3].trim();
            return new MenuItem(id, description, cost, category);

        } catch (RuntimeException ex) {
            System.out.println("invalid line(has been skipped): " + rawLine);
            return null;
        }
    }
}