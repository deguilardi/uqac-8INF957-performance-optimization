package ca.uqac.performance.original;

import static ca.uqac.performance.original.Config.DEBUG_MODE;

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
