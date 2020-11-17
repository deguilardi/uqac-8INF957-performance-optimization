package ca.uqac.performance.system;

import ca.uqac.performance.Transformer;
import ca.uqac.performance.Request;
import ca.uqac.performance.Supplier;

interface MyAdapterInterface {
    void preLoop(Integer i);
    void postLoop(Integer i);
    void loopProcess(Transformer withTransformer, Supplier forSupplier, Integer i);
    Boolean canPush(Request request, Supplier fromSupplier, Transformer toTransformer);
}
