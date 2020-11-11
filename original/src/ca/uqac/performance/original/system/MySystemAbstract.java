package ca.uqac.performance.original.system;

import ca.uqac.performance.original.Request;
import ca.uqac.performance.original.Supplier;
import ca.uqac.performance.original.Transformer;
import ca.uqac.performance.original.util.Sleeper;
import javafx.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import static ca.uqac.performance.original.Config.*;
import static ca.uqac.performance.original.Config.NUM_SUPPLIERS;
import static ca.uqac.performance.original.Debug.debug;
import static ca.uqac.performance.original.Debug.output;

public abstract class MySystemAbstract implements MySystemInterface{

    protected static MySystemInterface instance;
    protected static List< Pair<Transformer, Supplier> > transformers = new LinkedList<>();
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
        isRunning = true;
        output("Running MySystem... This might take a while. Wait, don't freak out!");
        for(Integer i = 0; i < NUM_LOOPS_TO_MAINTENANCE; i++){
            Sleeper.unsafeSleep(LOOP_INTERVAL);
            loop(i);
        }
        enterMaintenanceMode();
    }

    @Override
    public void addSupplier(Supplier supplier) {
        Transformer transformer = new Transformer(transformers.size());
        transformers.add(new Pair<>(transformer, supplier));
        output("Added supplier: "+ supplier.getId() + ", for transformer: " + transformer.getId());
    }

    @Override
    public void pushRequest(Request request, Supplier fromSupplier) {
        Transformer transformer = getTransformerFor(fromSupplier);
        transformer.pushRequest(request, fromSupplier);
    }

    protected void loop(Integer i) {
        debug("loop not implemented");
    }

    private void enterMaintenanceMode(){
        debug("entered maintenance mode");
        for(Pair<Transformer, Supplier> pair : transformers){
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
        for(Pair<Transformer, Supplier> pair : transformers){
            Supplier supplier = pair.getValue();
            supplier.logEventsSummary();
            requestCount += supplier.getRequestCount();
            successCount += supplier.getSuccessCount();
            rejectedCount += supplier.getRejectedCount();
            totalCostCount += supplier.getTotalCost();
        }

        output("");
        output("==================== MY SYSTEM RESULTS ====================");
        output("request avg    : " + (float) requestCount / NUM_SUPPLIERS);
        output("success avg    : " + (float) successCount / NUM_SUPPLIERS);
        output("rejected avg   : " + (float) rejectedCount / NUM_SUPPLIERS);
        output("lost avg       : " + (float) (requestCount - successCount - rejectedCount) / NUM_SUPPLIERS);
        output("total cost avg : " + (float) totalCostCount / NUM_SUPPLIERS);

        isRunning = false;
    }

    private Transformer getTransformerFor(Supplier supplier){
        for(Pair<Transformer, Supplier> pair : transformers){
            if(pair.getValue().equals(supplier)){
                return pair.getKey();
            }
        }
        return null;
    }
}
