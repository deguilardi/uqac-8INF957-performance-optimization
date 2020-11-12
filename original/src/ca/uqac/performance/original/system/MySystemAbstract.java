package ca.uqac.performance.original.system;

import ca.uqac.performance.original.Request;
import ca.uqac.performance.original.Supplier;
import ca.uqac.performance.original.Transformer;
import ca.uqac.performance.original.util.Sleeper;
import javafx.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static ca.uqac.performance.original.Config.*;
import static ca.uqac.performance.original.Config.NUM_SUPPLIERS;
import static ca.uqac.performance.original.Debug.debug;
import static ca.uqac.performance.original.Debug.output;
import static ca.uqac.performance.original.Transformer.State.DANGER;

public abstract class MySystemAbstract implements MySystemInterface{

    protected static MySystemInterface instance;
    protected static List< Pair<Transformer, Supplier> > suppliers = new LinkedList<>();
    private Boolean isRunning = false;

    protected MySystemAbstract(){
    }

    public static void initSystemWith(String className) {
        try {
            Class myClass = Class.forName(className);
            instance = (MySystemInterface) myClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static MySystemInterface systemInstance(){
        return instance;
    }

    @Override
    public void run(){
        if(isRunning){
            return;
        }

        new Thread(() -> {
            isRunning = true;
            output("Running MySystem... This might take a while. Wait, don't freak out!");
            for(Integer i = 0; i < NUM_LOOPS_TO_MAINTENANCE; i++){
                Sleeper.unsafeSleep(LOOP_INTERVAL);
                loop(i);
            }
            enterMaintenanceMode();
        }).start();
    }

    @Override
    public void setSuppliers(List<Pair<Transformer, Supplier>> suppliers) {
        this.suppliers = suppliers;
    }

    @Override
    public void pushRequest(Request request, Supplier fromSupplier) {
        Transformer transformer = getTransformerFor(fromSupplier);
        if(transformer.getState() == DANGER){
            request.overload();
        }
        transformer.pushRequest(request, fromSupplier);
    }

    protected void loop(Integer i) {
        debug("==================== loop " + i + " ====================");
        checkBalance();
    }

    private void checkBalance(){

        // Order transformers list by load
        // Has to be a clone, to not loose concurrent references
        List< Pair<Transformer, Supplier> > transformers = new ArrayList<>(this.suppliers);
        transformers.sort(Comparator.comparing(transformer -> transformer.getKey().getLoad()));

        for(Integer i = transformers.size(); i > transformers.size() / 2; i--){
            Pair<Transformer, Supplier> tailPair = transformers.get(i - 1);
            Pair<Transformer, Supplier> headPair = transformers.get(transformers.size() - i);
            checkBalancePairs(tailPair, headPair);
        }
    }

    private void checkBalancePairs(Pair<Transformer, Supplier> fromPair, Pair<Transformer, Supplier> toPair){
        Transformer from = fromPair.getKey();
        Transformer to = toPair.getKey();
        Integer loadDifference = from.getLoad() - to.getLoad();
        if(loadDifference >= OPTIMIZATION_BALANCE_THRESHOLD){
            debug("Unbalanced difference: " + loadDifference);
            for(Integer i = 0; i < loadDifference / 2; i++){
                Request request = from.peekRequest();
                if(request != null) {
                    request.unbalance();
                }
            }
        }
    }

    private void enterMaintenanceMode(){
        debug("entered maintenance mode");
        for(Pair<Transformer, Supplier> pair : suppliers){
            Supplier supplier = pair.getValue();
            supplier.interrupt();
            if(DEBUG_MODE) {
                supplier.logEventsDetails();
            }
        }
        Integer requestCount = 0;
        Integer successCount = 0;
        Integer rejectedCount = 0;
        Integer totalCostCount = 0;
        for(Pair<Transformer, Supplier> pair : suppliers){
            Supplier supplier = pair.getValue();
            supplier.logEventsSummary();
            requestCount += supplier.getRequestCount();
            successCount += supplier.getSuccessCount();
            rejectedCount += supplier.getRejectedCount();
            totalCostCount += supplier.getTotalCost();
        }

        output("");
        output("==================== MY SYSTEM RESULTS ====================");
        output("request avg  : " + String.format("%" + 10 + "s", requestCount / NUM_SUPPLIERS));
        output("success avg  : " + String.format("%" + 10 + "s", successCount / NUM_SUPPLIERS));
        output("rejected avg : " + String.format("%" + 10 + "s", rejectedCount / NUM_SUPPLIERS));
        output("lost avg     : " + String.format("%" + 10 + "s", (requestCount - successCount - rejectedCount) / NUM_SUPPLIERS));
        output("cost avg     : " + String.format("%" + 10 + "s", totalCostCount / NUM_SUPPLIERS));
        output("cost total   : " + String.format("%" + 10 + "s", totalCostCount));

        isRunning = false;
        System.exit(0);
    }

    protected Transformer getTransformerFor(Supplier supplier){
        for(Pair<Transformer, Supplier> pair : suppliers){
            if(pair.getValue().equals(supplier)){
                return pair.getKey();
            }
        }
        return null;
    }
}
