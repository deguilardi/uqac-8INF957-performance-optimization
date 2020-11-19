package ca.uqac.performance.system;

import ca.uqac.performance.Transformer;
import ca.uqac.performance.Request;
import ca.uqac.performance.Supplier;
import ca.uqac.performance.util.Sleeper;
import ca.uqac.performance.util.Stats;
import javafx.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static ca.uqac.performance.Config.*;
import static ca.uqac.performance.util.Debug.debug;
import static ca.uqac.performance.util.Debug.output;
import static ca.uqac.performance.util.Stats.format;

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
        Stats success = new Stats(COST_DEFAULT);
        Stats imbalanced = new Stats(COST_IMBALANCE);
        Stats overloaded = new Stats(COST_OVERLOAD);
        Stats rejected = new Stats(COST_REJECTION);
        Stats lost = new Stats();
        Stats totalCost = new Stats();
        int totalQtd = 0;

        for(Pair<Transformer, Supplier> supplierPair : supplierPairs){
            Supplier supplier = supplierPair.getValue();
            supplier.logEventsSummary();
            success.calc(supplier.getSuccessCount());
            imbalanced.calc(supplier.getImbalancedCount());
            overloaded.calc(supplier.getOverloadedCount());
            rejected.calc(supplier.getRejectedCount());
            lost.calc(supplier.getLostCount());
            totalCost.calc(supplier.getTotalCost());
            totalQtd += supplier.getRequestCount();
        }

        output("");
        output("============================= MY SYSTEM RESULTS ===============================");
        output("| responses  |    qtd.    | cost (tot) | cost (min) | cost (max) | cost (avg) |");
        output("| ---------- | ---------- | ---------- | ---------- | ---------- | ---------- |");
        output("| success    | " + format(success.tot()) + " | " + format(success.totCost()) + " | " + format(success.minCost()) + " | " + format(success.maxCost()) + " | " + format(success.avgCost()) + " |");
        output("|    imbala. | " + format(imbalanced.tot()) + " | " + format(imbalanced.totCost()) + " | " + format(imbalanced.minCost()) + " | " + format(imbalanced.maxCost()) + " | " + format(imbalanced.avgCost()) + " |");
        output("|    overlo. | " + format(overloaded.tot()) + " | " + format(overloaded.totCost()) + " | " + format(overloaded.minCost()) + " | " + format(overloaded.maxCost()) + " | " + format(overloaded.avgCost()) + " |");
        output("| rejected   | " + format(rejected.tot()) + " | " + format(rejected.totCost()) + " | " + format(rejected.minCost()) + " | " + format(rejected.maxCost()) + " | " + format(rejected.avgCost()) + " |");
        output("| lost       | " + format(lost.tot()) + " |     n/a    |     n/a    |     n/a    |     n/a    |");
        output("| total      | " + format(totalQtd) + " | " + format(totalCost.totCost()) + " | " + format(totalCost.minCost()) + " | " + format(totalCost.maxCost()) + " | " + format(totalCost.avgCost()) + " |");
        output("| ---------- | ---------- | ---------- | ---------- | ---------- | ---------- |");

        output("");
        output("======================= MY SYSTEM RESULTS (with tabs) ==========================");
        output(success.tot() + "\t" + success.totCost() + "\t" + success.minCost() + "\t" + success.maxCost() + "\t" + success.avgCost());
        output(imbalanced.tot() + "\t" + imbalanced.totCost() + "\t" + imbalanced.minCost() + "\t" + imbalanced.maxCost() + "\t" + imbalanced.avgCost());
        output(overloaded.tot() + "\t" + overloaded.totCost() + "\t" + overloaded.minCost() + "\t" + overloaded.maxCost() + "\t" + overloaded.avgCost());
        output(rejected.tot() + "\t" + rejected.totCost() + "\t" + rejected.minCost() + "\t" + rejected.maxCost() + "\t" + rejected.avgCost());
        output(lost.tot() + "\tn/a\tn/a\tn/a\tn/a");
        output(totalQtd + "\t" + totalCost.totCost() + "\t" + totalCost.minCost() + "\t" + totalCost.maxCost() + "\t" + totalCost.avgCost());
        output("--------------------------------------------------------------------------------");

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

    Pair<Transformer, Supplier> getSupplierPair(int index){
        return supplierPairs.get(index);
    }
}
