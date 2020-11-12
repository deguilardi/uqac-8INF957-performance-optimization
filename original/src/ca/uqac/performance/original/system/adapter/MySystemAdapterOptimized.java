package ca.uqac.performance.original.system.adapter;

import ca.uqac.performance.original.Request;
import ca.uqac.performance.original.Supplier;
import ca.uqac.performance.original.Transformer;
import ca.uqac.performance.original.system.MySystem;
import ca.uqac.performance.original.system.MySystemInterface;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ca.uqac.performance.original.Config.*;
import static ca.uqac.performance.original.util.Debug.debug;
import static ca.uqac.performance.original.Transformer.State.DANGER;
import static ca.uqac.performance.original.Transformer.State.OK;

/**
 * The optimized system.
 * It performs the optimizations set on Config.class.
 * Also, OPTIMIZED_MODE needs to be set to true for this system runs.
 */
@SuppressWarnings("unused")
public class MySystemAdapterOptimized extends MySystem implements MySystemInterface {

    // Control what suppliers are cooling down if OPTIMIZE_OVERLOAD is true
    private final boolean[] blockages = new boolean[NUM_SUPPLIERS];

    /**
     * System loop is called every LOOP_INTERVAL interval, in ms.
     * It will perform optimizations for each loop.
     * @param i Loop count.
     */
    @Override
    protected void loop(Integer i){
        if(OPTIMIZE_IMBALANCE) {
            balanceTransformers();
        }
        if(OPTIMIZE_OVERLOAD) {
            checkLoad();
        }
        super.loop(i);
        for(Pair<Transformer, Supplier> supplierPair : supplierPairs){
            Transformer transformer = supplierPair.getKey();
            Supplier supplier = supplierPair.getValue();
            transformer.processFor(supplier);
        }
    }

    /**
     * Override pushRequest to intercept requests from suppliers.
     * It will block requests in case transformers are in "danger".
     * @param request The request from fromSupplier.
     * @param fromSupplier The supplier requesting request.
     */
    @Override
    public void pushRequest(Request request, Supplier fromSupplier) {
        if(OPTIMIZE_OVERLOAD) {
            Transformer transformer = getTransformerFor(fromSupplier);
            if (blockages[transformer.getId()]) {
                request.reject();
                fromSupplier.receiveResponseFor(request);
            } else {
                super.pushRequest(request, fromSupplier);
            }
        }
        else{
            super.pushRequest(request, fromSupplier);
        }
    }

    /**
     * Balance transformers to keep them evenly loaded.
     */
    private void balanceTransformers(){

        // Order transformers list by load
        // Has to be a clone, to not loose concurrent references
        List< Pair<Transformer, Supplier> > transformers = new ArrayList<>(supplierPairs);
        transformers.sort(Comparator.comparing(transformer -> transformer.getKey().getLoad()));

        // Traverse transformers, comparing the the edges load differences
        for(int i = transformers.size(); i > transformers.size() / 2; i--){
            Pair<Transformer, Supplier> tailPair = transformers.get(i - 1);
            Pair<Transformer, Supplier> headPair = transformers.get(transformers.size() - i);
            balancePairs(tailPair, headPair);
        }
    }

    /**
     * Auxiliary method used by balanceTransformers
     * Will redistribute the load if buffer capacity difference is greater than OPTIMIZATION_IMBALANCE_THRESHOLD.
     * @param fromPair The pair with more load.
     * @param toPair The pair with less load.
     */
    private void balancePairs(Pair<Transformer, Supplier> fromPair, Pair<Transformer, Supplier> toPair){
        Transformer from = fromPair.getKey();
        Transformer to = toPair.getKey();
        int loadDifference = from.getLoad() - to.getLoad();
        if(loadDifference >= OPTIMIZATION_IMBALANCE_THRESHOLD){
            debug("Balancing difference: " + loadDifference);
            Supplier respondToSupplier = fromPair.getValue();
            for(int i = 0; i < loadDifference / 2; i++){
                Request request = from.pollRequest();
                if(request != null) {
                    to.pushRequest(request, respondToSupplier);
                }
            }
        }
    }

    /**
     * Check and block transformers with buffer load in "danger".
     * The danger trigger is set on TRANSFORMER_BUFFER_DANGER in Config.java.
     */
    private void checkLoad() {
        for(int i = 0; i < blockages.length; i++){
            boolean blocked = blockages[i];
            Transformer transformer = supplierPairs.get(i).getKey();
            if(blocked && transformer.getState() == OK){
                blockages[i] = false;
            } else if (transformer.getState() == DANGER){
                blockages[i] = true;
            }
        }
    }
}
