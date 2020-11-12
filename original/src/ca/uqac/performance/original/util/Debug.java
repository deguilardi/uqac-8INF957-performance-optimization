package ca.uqac.performance.original.util;

import static ca.uqac.performance.original.Config.DEBUG_MODE;

/**
 * Little helper to write debug or output
 */
public class Debug {
    public static void debug(String string){
        if(DEBUG_MODE){
            output(string);
        }
    }

    public static void output(String string){
        System.out.println(string);
    }
}
