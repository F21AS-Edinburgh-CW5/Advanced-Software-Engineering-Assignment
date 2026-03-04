package loader;
import model.OrderRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
AI usage instructions:
Ask AI about the format of the List container.
How to use BufferedReader to read files.
The usage of reading each line's function and the usage of processing each line's function for handling whitespace characters.

 */
public class OrderLoader {

     // CSV format : id, description, cost, category.If there are any illegal lines, such as incomplete data, ignore them
     
    public List<OrderRecord> load(String filename) {
        List<OrderRecord> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            for (String raw = reader.readLine(); raw != null; raw = reader.readLine()) {
                OrderRecord record = parseLine(raw);
                if (record != null) {
                    result.add(record);
                }
            }
        } catch (IOException io) {
            System.out.println("Error!cannt read the file(order).  Name: " + filename);
        }

        return result;
    }

    //read menu,return:MenuItem Object

    private OrderRecord parseLine(String rawLine) {
        if (rawLine == null) return null;

        String s = rawLine.trim();
        if (s.isEmpty()) return null;

        String[] fields = s.split(",", -1);
        if (fields.length < 3) {
            System.out.println("Skipped order line: " + rawLine);
            return null;
        }

        try {
            String time = fields[0].trim();
            String custID= fields[1].trim();
            String itemId = fields[2].trim();

            return new OrderRecord(time, custID, itemId);
        } catch (RuntimeException ex) {
            System.out.println("[OrderLoader] Skipped invalid line: " + rawLine);
            return null;
        }
    }
}