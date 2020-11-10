package ca.uqac.performance.original;

import static ca.uqac.performance.original.Config.NUM_SUPPLIERS;
import static ca.uqac.performance.original.MySystem.systemInstance;

public class Main {

    public static void main(String[] args){
        initSuppliers();
        systemInstance().run();
    }

    private static void initSuppliers(){
        for(Integer i = 0; i < NUM_SUPPLIERS; i++){
            Supplier supplier = new Supplier(i);
            systemInstance().addSupplier(supplier);
            supplier.start();
        }
    }

}
