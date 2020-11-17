package ca.uqac.performance.system;

import ca.uqac.performance.Transformer;
import ca.uqac.performance.Request;
import ca.uqac.performance.Supplier;
import javafx.util.Pair;

import java.util.List;

/**
 * Interface that defines MySystem contract to Suppliers, Transformers and etc.
 */
public interface MySystemInterface {
    void run();
    void set(List<Pair<Transformer, Supplier>> supplier);
    void push(Request request, Supplier fromSupplier);
}
