package se.vidstige.jadb.managers;

public class Bash {
    private Bash() {
        throw new IllegalStateException("Utility class");
    }

    public static String quote(String s) {
        // Check that s contains no whitespace
        if (s.matches("\\S+")) {
            return s;
        }
        return "'" + s.replace("'", "'\\''") + "'";
    }
}
