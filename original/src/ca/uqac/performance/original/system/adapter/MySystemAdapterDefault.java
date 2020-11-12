package ca.uqac.performance.original.system.adapter;

import ca.uqac.performance.original.Supplier;
import ca.uqac.performance.original.Transformer;
import ca.uqac.performance.original.system.MySystem;
import ca.uqac.performance.original.system.MySystemInterface;
import javafx.util.Pair;

/**
 * The default (non-optimized) system.
 * It basically runs the system loops to trigger transformers' processes.
 * Also, OPTIMIZED_MODE needs to be set to false for this system runs.
 */
@SuppressWarnings("unused")
public class MySystemAdapterDefault extends MySystem implements MySystemInterface {

    /**
     * System loop is called every LOOP_INTERVAL interval, in ms.
     * @param i Loop count.
     */
    @Override
    protected void loop(Integer i){
        super.loop(i);
        for(Pair<Transformer, Supplier> supplierPair : supplierPairs){
            Transformer transformer = supplierPair.getKey();
            Supplier supplier = supplierPair.getValue();
            transformer.processFor(supplier);
        }
    }
}
