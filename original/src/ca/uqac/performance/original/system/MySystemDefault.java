package ca.uqac.performance.original.system;

import ca.uqac.performance.original.Supplier;
import ca.uqac.performance.original.Transformer;
import javafx.util.Pair;

import static ca.uqac.performance.original.Debug.debug;

@SuppressWarnings("unused")
public class MySystemDefault extends MySystemAbstract implements MySystemInterface {

    @Override
    protected void loop(Integer i){
        debug("==================== loop " + i + " ====================");
        for(Pair<Transformer, Supplier> pair : transformers){
            Transformer transformer = pair.getKey();
            Supplier supplier = pair.getValue();
            transformer.processFor(supplier);
        }
    }
}
