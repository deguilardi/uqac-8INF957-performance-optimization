package ca.uqac.performance;

import ca.uqac.performance.util.Sleeper;
import ca.uqac.performance.system.MySystem;
import ca.uqac.performance.util.Debug;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static ca.uqac.performance.Config.*;

public class Supplier extends Thread{

    private Integer id;
    private ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<>();
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger imbalancedCount = new AtomicInteger(0);
    private AtomicInteger overloadedCount = new AtomicInteger(0);
    private AtomicInteger rejectedCount = new AtomicInteger(0);
    private AtomicInteger totalCost = new AtomicInteger(0);

    public Supplier(Integer id) {
        this.id = id;
    }

    @Override
    public void run() {
        super.run();

        while(true){
            if(!Sleeper.unsafeSleep(Config.REQUEST_INTERVAL)){
                break;
            }
            generateRequest();
        }
    }

    void generateRequest(){
        Request request = new Request();
        requests.add(request);
        MySystem.systemInstance().push(request, this);
    }

    public void receiveResponseFor(Request request){
        totalCost.set(totalCost.get() + request.getCost());
        if(request.isSuccessful()){
            successCount.set(successCount.get() + 1);
            if(request.isUnbalanced()){
                imbalancedCount.set(imbalancedCount.get() + 1);
            }
            if(request.isOverloaded()){
                overloadedCount.set(overloadedCount.get() + 1);
            }
        } else {
            rejectedCount.set(rejectedCount.get() + 1);
        }
    }

    public void logEventsDetails(){
        Debug.output("==================== LOGGING EVENTS FOR SUPPLIER #" +id+ " ====================");
        for(Request request : requests){
            Debug.output(request.toString());
        }
    }

    public void logEventsSummary(){
        Debug.output("");
        Debug.output("== SUMMARY OF EVENTS FOR SUPPLIER #" +id+ " ==");
        Debug.output("| responses  |    qtd.    |    cost    |");
        Debug.output("| ---------- | ---------- | ---------- |");
        Debug.output("| success    | " + String.format("%" + 10 + "s", successCount) + " | " + String.format("%" + 10 + "s", successCount) + " |");
        Debug.output("|    imbala. | " + String.format("%" + 10 + "s", imbalancedCount) + " | " + String.format("%" + 10 + "s", imbalancedCount.get() * COST_IMBALANCE) + " |");
        Debug.output("|    overlo. | " + String.format("%" + 10 + "s", overloadedCount) + " | " + String.format("%" + 10 + "s", overloadedCount.get() * COST_OVERLOAD) + " |");
        Debug.output("| rejected   | " + String.format("%" + 10 + "s", rejectedCount) + " | " + String.format("%" + 10 + "s", rejectedCount.get() * COST_REJECTION) + " |");
        Debug.output("| lost       | " + String.format("%" + 10 + "s", getLostCount()) + " |     n/a    |");
        Debug.output("| total      | " + String.format("%" + 10 + "s", requests.size()) + " | " + String.format("%" + 10 + "s", totalCost) + " |");
        Debug.output("| ---------- | ---------- | ---------- |");
    }

    public Integer getRequestCount() {
        return requests.size();
    }

    public Integer getSuccessCount() {
        return successCount.get();
    }

    public Integer getImbalancedCount() {
        return imbalancedCount.get();
    }

    public Integer getOverloadedCount() {
        return overloadedCount.get();
    }

    public Integer getRejectedCount() {
        return rejectedCount.get();
    }

    public Integer getLostCount() {
        Integer lostCount = 0;
        for(Request request : requests){
            if (request.isLost()){
                lostCount++;
            }
        }
        return lostCount;
    }

    public Integer getTotalCost(){
        return totalCost.get();
    }
}
