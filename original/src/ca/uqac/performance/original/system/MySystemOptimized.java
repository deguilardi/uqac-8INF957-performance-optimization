package ca.uqac.performance.original.system;

import ca.uqac.performance.original.Request;
import ca.uqac.performance.original.Supplier;
import ca.uqac.performance.original.Transformer;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ca.uqac.performance.original.Config.OPTIMIZATION_BALANCE_THRESHOLD;
import static ca.uqac.performance.original.Debug.debug;

@SuppressWarnings("unused")
public class MySystemOptimized extends MySystemAbstract implements MySystemInterface {

    @Override
    protected void loop(Integer i){
        balanceTransformers();
        super.loop(i);
        for(Pair<Transformer, Supplier> pair : suppliers){
            Transformer transformer = pair.getKey();
            Supplier supplier = pair.getValue();
            transformer.processFor(supplier);
        }
    }

    private void balanceTransformers(){

        // Order transformers list by load
        // Has to be a clone, to not loose concurrent references
        List< Pair<Transformer, Supplier> > transformers = new ArrayList<>(this.suppliers);
        transformers.sort(Comparator.comparing(transformer -> transformer.getKey().getLoad()));

        // Traverse transformers, comparing the the edges load differences
        for(Integer i = transformers.size(); i > transformers.size() / 2; i--){
            Pair<Transformer, Supplier> tailPair = transformers.get(i - 1);
            Pair<Transformer, Supplier> headPair = transformers.get(transformers.size() - i);
            balancePairs(tailPair, headPair);
        }
    }

    private void balancePairs(Pair<Transformer, Supplier> fromPair, Pair<Transformer, Supplier> toPair){
        Transformer from = fromPair.getKey();
        Transformer to = toPair.getKey();
        Integer loadDifference = from.getLoad() - to.getLoad();
        if(loadDifference >= OPTIMIZATION_BALANCE_THRESHOLD){
            debug("Balancing difference: " + loadDifference);
            Supplier respondToSupplier = fromPair.getValue();
            for(Integer i = 0; i < loadDifference / 2; i++){
                Request request = from.pollRequest();
                if(request != null) {
                    to.pushRequest(request, respondToSupplier);
                }
            }
        }
    }
}
