package ca.uqac.performance.original.system;

import ca.uqac.performance.original.Request;
import ca.uqac.performance.original.Supplier;

public interface MySystemInterface {
    void run();
    void addSupplier(Supplier supplier);
    void pushRequest(Request request, Supplier fromSupplier);
}
