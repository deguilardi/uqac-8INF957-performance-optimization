package ca.uqac.performance.original;

import javafx.util.Pair;

import java.util.LinkedList;
import java.util.List;

import static ca.uqac.performance.original.Config.NUM_SUPPLIERS;
import static ca.uqac.performance.original.Config.OPTIMIZED_MODE;
import static ca.uqac.performance.original.Debug.output;
import static ca.uqac.performance.original.Gui.initGui;
import static ca.uqac.performance.original.system.MySystemAbstract.initSystemWith;
import static ca.uqac.performance.original.system.MySystemAbstract.systemInstance;

public class Main {
    protected static List< Pair<Transformer, Supplier> > suppliers = new LinkedList<>();

    public static void main(String[] args){
        initSystem();
        initSuppliers();
        initGui(suppliers);
        systemInstance().run();
        startSuppliers();
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
            Transformer transformer = new Transformer(suppliers.size());
            Supplier supplier = new Supplier(i);
            suppliers.add(new Pair<>(transformer, supplier));
            output("Added supplier: "+ supplier.getId() + ", for transformer: " + transformer.getId());
        }

        systemInstance().setSuppliers(suppliers);
    }

    private static void startSuppliers(){
        for(Pair<Transformer, Supplier> pair : suppliers){
            Supplier supplier = pair.getValue();
            supplier.start();
        }
    }
}
