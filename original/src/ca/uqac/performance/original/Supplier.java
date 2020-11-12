package ca.uqac.performance.original;

import ca.uqac.performance.original.util.Sleeper;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static ca.uqac.performance.original.Config.*;
import static ca.uqac.performance.original.Debug.output;
import static ca.uqac.performance.original.system.MySystemAbstract.systemInstance;

public class Supplier extends Thread{

    private Integer id;
    private ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<>();
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger rejectedCount = new AtomicInteger(0);
    private AtomicInteger totalCost = new AtomicInteger(0);

    public Supplier(Integer id) {
        this.id = id;
    }

    @Override
    public void run() {
        super.run();

        while(true){
            if(!Sleeper.unsafeSleep(REQUEST_INTERVAL)){
                break;
            }
            generateRequest();
        }
    }

    void generateRequest(){
        Request request = new Request();
        requests.add(request);
        systemInstance().pushRequest(request, this);
    }

    public void receiveResponseFor(Request request){
        totalCost.set(totalCost.get() + request.getCost());
        if(request.isSuccessful()){
            successCount.set(successCount.get() + 1);
        } else {
            rejectedCount.set(rejectedCount.get() + 1);
        }
    }

    public void logEventsDetails(){
        output("==================== LOGGING EVENTS FOR SUPPLIER #" +id+ " ====================");
        for(Request request : requests){
            output(request.toString());
        }
    }

    public void logEventsSummary(){
        Integer numRequests = requests.size();
        Integer successCount = 0;
        Integer successCost = 0;
        Integer inbalanceCount = 0;
        Integer inbalanceCost = 0;
        Integer overloadCount = 0;
        Integer overloadCost = 0;
        Integer rejectedCount = 0;
        Integer rejectedCost = 0;
        Integer lostCount = 0;
        Integer totalCost = 0;

        for(Request request : requests){
            if (request.isLost()){
                lostCount++;
            } else if(request.isSuccessful()){
                if(request.isUnbalanced()){
                    inbalanceCount++;
                    inbalanceCost += COST_IMBALANCE;
                }
                if(request.isOverloaded()){
                    overloadCount++;
                    overloadCost += COST_OVERLOAD;
                }
                successCount++;
                successCost += 1;
                totalCost += request.getCost();
            }
            else{
                rejectedCount++;
                rejectedCost += request.getCost();
                totalCost += request.getCost();
            }
        }

        output("");
        output("==== SUMMARY OF EVENTS FOR SUPPLIER #" +id+ " ====");
        output("|     type      |    qtd.    |    cost    |");
        output("| ------------- | ---------- | ---------- |");
        output("| default cost  | " + String.format("%" + 10 + "s", successCount) + " | " + String.format("%" + 10 + "s", successCost) + " |");
        output("| inbalan. cost | " + String.format("%" + 10 + "s", inbalanceCount) + " | " + String.format("%" + 10 + "s", inbalanceCost) + " |");
        output("| overload cost | " + String.format("%" + 10 + "s", overloadCount) + " | " + String.format("%" + 10 + "s", overloadCost) + " |");
        output("| rejected      | " + String.format("%" + 10 + "s", rejectedCount) + " | " + String.format("%" + 10 + "s", rejectedCost) + " |");
        output("| lost          | " + String.format("%" + 10 + "s", lostCount) + " |     n/a    |");
        output("| total         | " + String.format("%" + 10 + "s", numRequests) + " | " + String.format("%" + 10 + "s", totalCost) + " |");
        output("| ------------- | ---------- | ---------- |");
    }

    public Integer getRequestCount() {
        return requests.size();
    }

    public Integer getSuccessCount() {
        return successCount.get();
    }

    public Integer getRejectedCount() {
        return rejectedCount.get();
    }

    public Integer getTotalCost(){
        return totalCost.get();
    }
}
