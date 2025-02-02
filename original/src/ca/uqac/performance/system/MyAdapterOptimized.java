package ca.uqac.performance.system;

import ca.uqac.performance.Transformer;
import ca.uqac.performance.Request;
import ca.uqac.performance.Supplier;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ca.uqac.performance.Config.*;
import static ca.uqac.performance.Transformer.State.*;
import static ca.uqac.performance.util.Debug.debug;

/**
 * The optimized system.
 * It performs the optimizations set on Config.class.
 * Also, OPTIMIZED_MODE needs to be set to true for this system runs.
 */
@SuppressWarnings("unused")
public class MyAdapterOptimized implements MyAdapterInterface {

    private Integer currentLoop = -1;
    List< Pair<Transformer, Supplier> > supplierPairsCopy;

    /**
     * Adapter pre loop is called every LOOP_INTERVAL interval, in ms.
     * @param i Loop count.
     */
    @Override
    public void preLoop(Integer i) {
        if(currentLoop != i){
            supplierPairsCopy = new ArrayList<>(NUM_SUPPLIERS);
        }
    }

    /**
     * Adapter post loop is called every LOOP_INTERVAL interval, in ms.
     * @param i Loop count.
     */
    @Override
    public void postLoop(Integer i) {
        balanceTransformers();
    }

    /**
     * Adapter loop pair is called every LOOP_INTERVAL interval, in ms, per supplier pair.
     * @param withTransformer Transformer
     * @oaran forSupplier Supplier
     * @param i Loop count.
     */
    @Override
    public void loopProcess(Transformer withTransformer, Supplier forSupplier, Integer i) {
        withTransformer.processFor(forSupplier);
        supplierPairsCopy.add(new Pair<>(withTransformer, forSupplier));
    }

    /**
     * Called when a request is pushed from suppliers.
     * It will block requests in case transformers are in "danger".
     * @param request The request from fromSupplier.
     * @param fromSupplier The supplier requesting request.
     */
    @Override
    public Boolean canPush(Request request, Supplier fromSupplier, Transformer toTransformer) {
        return toTransformer.getState() != MAX && toTransformer.getState() != DANGER;
    }

    /**
     * Balance transformers to keep them evenly loaded.
     */
    private void balanceTransformers(){

        // Order transformers list by load
        // Has to be a clone, to not loose concurrent references
        supplierPairsCopy.sort(Comparator.comparing(transformer -> transformer.getKey().getLoad()));

        // Traverse transformers, comparing the the edges load differences
        for(int i = supplierPairsCopy.size(); i > supplierPairsCopy.size() / 2; i--){
            Pair<Transformer, Supplier> tailPair = supplierPairsCopy.get(i - 1);
            Pair<Transformer, Supplier> headPair = supplierPairsCopy.get(supplierPairsCopy.size() - i);
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
        if(loadDifference >= IMBALANCE_THRESHOLD){
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
}
