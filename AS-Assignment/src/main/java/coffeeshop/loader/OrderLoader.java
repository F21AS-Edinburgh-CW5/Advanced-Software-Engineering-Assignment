package coffeeshop.loader;

import coffeeshop.model.OrderRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrderLoader {

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
            System.out.println("Error! Cannot read file (order): " + filename);
        }

        return result;
    }

    private OrderRecord parseLine(String rawLine) {
        if (rawLine == null) return null;
        String s = rawLine.trim();
        if (s.isEmpty()) return null;

        String[] fields = s.split(",", -1);
        if (fields.length < 3) {
            System.out.println("[OrderLoader] Skipped order line: " + rawLine);
            return null;
        }

        try {
            String time = fields[0].trim();
            String custID = fields[1].trim();
            String itemId = fields[2].trim();
            return new OrderRecord(time, custID, itemId);
        } catch (RuntimeException ex) {
            System.out.println("[OrderLoader] Skipped invalid line: " + rawLine);
            return null;
        }
    }
}
