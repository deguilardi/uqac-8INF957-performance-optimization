package ca.uqac.performance.original;

import static ca.uqac.performance.original.Config.DEBUG_MODE;

public class Debug {

    static void debug(String string){
        if(DEBUG_MODE){
            output(string);
        }
    }

    static void output(String string){
        System.out.println(string);
    }

}
