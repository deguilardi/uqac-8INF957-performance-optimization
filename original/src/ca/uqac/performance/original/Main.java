package ca.uqac.performance.original;

import ca.uqac.performance.original.system.MySystemDefault;
import ca.uqac.performance.original.system.MySystemOptimized;

import static ca.uqac.performance.original.Config.NUM_SUPPLIERS;
import static ca.uqac.performance.original.Config.OPTIMIZED_MODE;
import static ca.uqac.performance.original.system.MySystemAbstract.systemInstance;

public class Main {

    public static void main(String[] args){
        initSystem();
        initSuppliers();
        systemInstance().run();
    }

    private static void initSystem(){
        if(OPTIMIZED_MODE) {
            MySystemOptimized.init();
        } else {
            MySystemDefault.init();
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
