package ca.uqac.performance.original;

import ca.uqac.performance.original.util.Sleeper;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static ca.uqac.performance.original.Config.REQUEST_INTERVAL;
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

    void receiveResponseFor(Request request){
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
        Integer successDefCount = 0;
        Integer successDefCost = 0;
        Integer successUmbCount = 0;
        Integer successUmbCost = 0;
        Integer rejectedCount = 0;
        Integer rejectedCost = 0;
        Integer lostCount = 0;
        Integer totalCost = 0;

        for(Request request : requests){
            if (request.isLost()){
                lostCount++;
            } else if(request.isSuccessful()){
                if(request.isUnbalanced()){
                    successUmbCount++;
                    successUmbCost += request.getCost();
                }
                else {
                    successDefCount++;
                    successDefCost += request.getCost();
                }
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
        output("| success (def) | " + String.format("%" + 10 + "s", successDefCount) + " | " + String.format("%" + 10 + "s", successDefCost) + " |");
        output("| success (umb) | " + String.format("%" + 10 + "s", successUmbCount) + " | " + String.format("%" + 10 + "s", successUmbCost) + " |");
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
