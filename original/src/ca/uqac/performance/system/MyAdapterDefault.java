package ca.uqac.performance.system;

import ca.uqac.performance.Transformer;
import ca.uqac.performance.Request;
import ca.uqac.performance.Supplier;

import static sun.security.krb5.internal.LoginOptions.MAX;

/**
 * The default (non-optimized) system.
 * It basically runs the system loops to trigger transformers' processes.
 * Also, OPTIMIZED_MODE needs to be set to false for this system runs.
 */
@SuppressWarnings("unused")
public class MyAdapterDefault implements MyAdapterInterface {

    /**
     * Adapter pre loop is called every LOOP_INTERVAL interval, in ms.
     * @param i Loop count.
     */
    @Override
    public void preLoop(Integer i) {
        // This adapter doesn't implement this method
    }

    /**
     * Adapter post loop is called every LOOP_INTERVAL interval, in ms.
     * @param i Loop count.
     */
    @Override
    public void postLoop(Integer i) {
        // This adapter doesn't implement this method
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
    }

    /**
     * Determines if a new request can be pushed.
     * @param request The request from fromSupplier.
     * @param fromSupplier The supplier requesting request.
     */
    @Override
    public Boolean canPush(Request request, Supplier fromSupplier, Transformer toTransformer) {
        return toTransformer.getLoad() != MAX;
    }
}
