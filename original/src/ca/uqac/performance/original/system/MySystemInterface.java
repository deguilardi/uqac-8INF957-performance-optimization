package ca.uqac.performance.original.system;

import ca.uqac.performance.original.Request;
import ca.uqac.performance.original.Supplier;
import ca.uqac.performance.original.Transformer;
import javafx.util.Pair;

import java.util.List;

public interface MySystemInterface {
    void run();
    void setSuppliers(List<Pair<Transformer, Supplier>> supplier);
    void pushRequest(Request request, Supplier fromSupplier);
}
