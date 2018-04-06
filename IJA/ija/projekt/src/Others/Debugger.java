package Others;
import java.lang.System;

public class Debugger {
    static boolean enabled = true;
    public static void log(Object o) {
        System.out.println(o.toString());
    }
}
