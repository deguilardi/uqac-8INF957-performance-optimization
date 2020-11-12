package ca.uqac.performance.original;

import javafx.util.Pair;

import java.util.LinkedList;
import java.util.List;

import static ca.uqac.performance.original.Config.NUM_SUPPLIERS;
import static ca.uqac.performance.original.Config.OPTIMIZED_MODE;
import static ca.uqac.performance.original.util.Debug.output;
import static ca.uqac.performance.original.Gui.initGui;
import static ca.uqac.performance.original.system.MySystem.initSystemWith;
import static ca.uqac.performance.original.system.MySystem.systemInstance;

/**
 * Main class.
 * Run this class to start the process.
 */
public class Main {

    // Pairs Transformers-Suppliers are hold here
    // As required by documentation, suppliers have a 1:1 relation to transformers.
    protected static List< Pair<Transformer, Supplier> > supplierPairs = new LinkedList<>();

    /**
     * The main start method.
     * Run this method to start the process.
     */
    public static void main(String[] args){
        initSystem();
        initSupplierPairs();
        systemInstance().setSuppliers(supplierPairs);
        initGui(supplierPairs);
        systemInstance().run();
        startSuppliers();
    }

    /**
     * Will start the system based on OPTIMIZED_MODE.
     * The OPTIMIZED_MODE can be set in Config.class.
     */
    private static void initSystem(){
        if(OPTIMIZED_MODE) {
            initSystemWith("ca.uqac.performance.original.system.adapter.MySystemAdapterOptimized");
        } else {
            initSystemWith("ca.uqac.performance.original.system.adapter.MySystemAdapterDefault");
        }
    }

    /**
     * Init supplier pairs based on NUM_SUPPLIERS.
     * The NUM_SUPPLIERS can be set in Config.class.
     */
    private static void initSupplierPairs(){
        for(Integer i = 0; i < NUM_SUPPLIERS; i++){
            Transformer transformer = new Transformer(supplierPairs.size());
            Supplier supplier = new Supplier(i);
            supplierPairs.add(new Pair<>(transformer, supplier));
            output("Added supplier: "+ supplier.getId() + ", for transformer: " + transformer.getId());
        }
    }

    /**
     * Start suppliers will start their loops to send requests in a timely basis.
     */
    private static void startSuppliers(){
        for(Pair<Transformer, Supplier> pair : supplierPairs){
            Supplier supplier = pair.getValue();
            supplier.start();
        }
    }
}
