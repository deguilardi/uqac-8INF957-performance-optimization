package ca.uqac.performance.original.system;

import ca.uqac.performance.original.Supplier;
import ca.uqac.performance.original.Transformer;
import javafx.util.Pair;

import static ca.uqac.performance.original.Debug.debug;

public class MySystemDefault extends MySystemAbstract implements MySystemInterface {

    public static void init(){
        if(instance == null){
            instance = new MySystemDefault();
        }
    }

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
