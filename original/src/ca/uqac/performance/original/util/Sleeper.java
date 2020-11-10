package ca.uqac.performance.original.util;

public class Sleeper {
    public static boolean unsafeSleep(long ms){
        try {
            Thread.sleep(ms);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
