package ca.uqac.performance.system;

import ca.uqac.performance.Transformer;
import ca.uqac.performance.Request;
import ca.uqac.performance.Supplier;
import ca.uqac.performance.util.Sleeper;
import javafx.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static ca.uqac.performance.Config.*;
import static ca.uqac.performance.Config.NUM_SUPPLIERS;
import static ca.uqac.performance.util.Debug.debug;
import static ca.uqac.performance.util.Debug.output;

public class MySystem implements MySystemInterface{

    protected static MySystemInterface instance;
    protected static List< Pair<Transformer, Supplier> > supplierPairs = new LinkedList<>();
    private Boolean isRunning = false;
    private final MyAdapterInterface adapter;

    /**
     * Block this class to be initialized.
     */
    private MySystem(MyAdapterInterface adapter){
        this.adapter = adapter;
    }

    /**
     * initialize singleton with the "adapter" defined by the className.
     * Utilize reflexion to perform initialization.
     * @param adapterClassName Adapter's full lass name (eg. "com.company.package.ClassName").
     */
    public static void initSystemWith(String adapterClassName) {
        try {
            Class<?> adapterClass = Class.forName(adapterClassName);
            MyAdapterInterface adapter = (MyAdapterInterface) adapterClass.getDeclaredConstructor().newInstance();
            instance = new MySystem(adapter);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Simply return singleton instance
     * @return Singleton instance.
     */
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
    public void set(List<Pair<Transformer, Supplier>> supplierPairs) {
        MySystem.supplierPairs = supplierPairs;
    }

    @Override
    public void push(Request request, Supplier fromSupplier) {
        Transformer toTransformer = getTransformerFor(fromSupplier);
        Boolean canPushRequest = adapter.canPush(request, fromSupplier, toTransformer);
        if (!canPushRequest) {
            request.reject();
            fromSupplier.receiveResponseFor(request);
            return;
        } else if (toTransformer.getState() == Transformer.State.DANGER) {
            request.overload();
        }
        toTransformer.pushRequest(request, fromSupplier);
    }

    private void loop(Integer i) {
        debug("==================== loop " + i + " ====================");
        adapter.preLoop(i);
        checkImbalance();
        for(Pair<Transformer, Supplier> supplierPair : supplierPairs){
            Transformer transformer = supplierPair.getKey();
            Supplier supplier = supplierPair.getValue();
            adapter.loopProcess(transformer, supplier, i);
        }
        adapter.postLoop(i);
    }

    private void checkImbalance(){

        // Order transformers list by load
        // Has to be a clone, to not loose concurrent references
        List< Pair<Transformer, Supplier> > transformers = new ArrayList<>(supplierPairs);
        transformers.sort(Comparator.comparing(transformer -> transformer.getKey().getLoad()));

        for(int i = transformers.size(); i > transformers.size() / 2; i--){
            Pair<Transformer, Supplier> tailPair = transformers.get(i - 1);
            Pair<Transformer, Supplier> headPair = transformers.get(transformers.size() - i);
            checkUnbalancedPairs(tailPair, headPair);
        }
    }

    private void checkUnbalancedPairs(Pair<Transformer, Supplier> fromPair, Pair<Transformer, Supplier> toPair){
        Transformer from = fromPair.getKey();
        Transformer to = toPair.getKey();
        int loadDifference = from.getLoad() - to.getLoad();
        if(loadDifference >= IMBALANCE_THRESHOLD){
            debug("Unbalanced difference: " + loadDifference);
            for(int i = 0; i < loadDifference / 2; i++){
                Request request = from.peekRequest();
                if(request != null) {
                    request.unbalance();
                }
            }
        }
    }

    private void enterMaintenanceMode(){
        debug("entered maintenance mode");
        for(Pair<Transformer, Supplier> supplierPair : supplierPairs){
            Supplier supplier = supplierPair.getValue();
            supplier.interrupt();
            if(DEBUG_MODE) {
                supplier.logEventsDetails();
            }
        }
        Integer requestCount = 0;
        Integer successCount = 0;
        Integer rejectedCount = 0;
        Integer totalCostCount = 0;
        for(Pair<Transformer, Supplier> supplierPair : supplierPairs){
            Supplier supplier = supplierPair.getValue();
            supplier.logEventsSummary();
            requestCount += supplier.getRequestCount();
            successCount += supplier.getSuccessCount();
            rejectedCount += supplier.getRejectedCount();
            totalCostCount += supplier.getTotalCost();
        }

        output("");
        output("============ MY SYSTEM RESULTS ============");
        output("request avg  : " + String.format("%" + 10 + "s", requestCount / NUM_SUPPLIERS));
        output("success avg  : " + String.format("%" + 10 + "s", successCount / NUM_SUPPLIERS));
        output("rejected avg : " + String.format("%" + 10 + "s", rejectedCount / NUM_SUPPLIERS));
        output("lost avg     : " + String.format("%" + 10 + "s", (requestCount - successCount - rejectedCount) / NUM_SUPPLIERS));
        output("cost avg     : " + String.format("%" + 10 + "s", totalCostCount / NUM_SUPPLIERS));
        output("cost total   : " + String.format("%" + 10 + "s", totalCostCount));

        isRunning = false;
        System.exit(0);
    }

    private Transformer getTransformerFor(Supplier supplier){
        for(Pair<Transformer, Supplier> supplierPair : supplierPairs){
            if(supplierPair.getValue().equals(supplier)){
                return supplierPair.getKey();
            }
        }
        return null;
    }
}
