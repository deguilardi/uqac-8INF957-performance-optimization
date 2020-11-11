package ca.uqac.performance.original;

import static ca.uqac.performance.original.Config.NUM_SUPPLIERS;
import static ca.uqac.performance.original.Config.OPTIMIZED_MODE;
import static ca.uqac.performance.original.system.MySystemAbstract.initSystemWith;
import static ca.uqac.performance.original.system.MySystemAbstract.systemInstance;

public class Main {

    public static void main(String[] args){
        initSystem();
        initSuppliers();
        systemInstance().run();
    }

    private static void initSystem(){
        if(OPTIMIZED_MODE) {
            initSystemWith("ca.uqac.performance.original.system.MySystemOptimized");
        } else {
            initSystemWith("ca.uqac.performance.original.system.MySystemDefault");
        }
    }

    private static void initSuppliers(){
        for(Integer i = 0; i < NUM_SUPPLIERS; i++){
            Supplier supplier = new Supplier(i);
            systemInstance().addSupplier(supplier);
            supplier.start();
        }
    }

}
