package ca.uqac.performance.util;

/**
 * Little helper to sleep threads.
 */
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
