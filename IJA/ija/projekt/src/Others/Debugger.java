package Others;
import java.lang.System;

public class Debugger {
    static boolean enabled = true;

    /**
     * Prints any object to console
     * @param o printable object
     */
    public static void log(Object o) {
        System.out.println(o.toString());
    }
}
