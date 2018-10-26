package se.vidstige.jadb.managers;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class which works with properties, uses getprop and setprop methods of android shell
 */
public class PropertyManager {
    private final Pattern pattern = Pattern.compile("^\\[([a-zA-Z0-9_.-]*)]:.\\[([^\\[\\]]*)]");
    private final JadbDevice device;

    public PropertyManager(JadbDevice device) {
        this.device = device;
    }

    public Map<String, String> getprop() throws IOException, JadbException {
        try (BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(device.executeShell("getprop"), StandardCharsets.UTF_8))) {
            return parseProp(bufferedReader);
        }
    }

    private Map<String, String> parseProp(BufferedReader bufferedReader) throws IOException {
        HashMap<String, String> result = new HashMap<>();

        String line;
        Matcher matcher = pattern.matcher("");

        while ((line = bufferedReader.readLine()) != null) {
            matcher.reset(line);

            if (matcher.find()) {
                if (matcher.groupCount() < 2) {
                    System.err.println("Property line: " + line + " does not match pattern. Ignoring");
                    continue;
                }
                String key = matcher.group(1);
                String value = matcher.group(2);
                result.put(key, value);
            }
        }

        return result;
    }
}
