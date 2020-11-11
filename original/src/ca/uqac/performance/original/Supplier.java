package ca.uqac.performance.original;

import ca.uqac.performance.original.system.MySystemDefault;
import ca.uqac.performance.original.util.Sleeper;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static ca.uqac.performance.original.Config.REQUEST_INTERVAL;
import static ca.uqac.performance.original.Debug.output;
import static ca.uqac.performance.original.system.MySystemDefault.systemInstance;

public class Supplier extends Thread{

    private Integer id;
    private ConcurrentLinkedQueue<Response> responses = new ConcurrentLinkedQueue<>();
    private AtomicInteger requestCount = new AtomicInteger(0);
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger rejectedCount = new AtomicInteger(0);

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
        MySystemDefault.systemInstance().pushRequest(new Request(), this);
        requestCount.set(requestCount.get() + 1);
    }

    void receiveResponse(Response response){
        responses.add(response);
        if(response.isSuccessful()){
            successCount.set(successCount.get() + 1);
        } else {
            rejectedCount.set(rejectedCount.get() + 1);
        }
    }

    public void logEventsDetails(){
        output("==================== LOGGING EVENTS FOR SUPPLIER #" +id+ " ====================");
        for(Response response : responses){
            output(response.toString());
        }
    }

    public void logEventsSummary(){
        output("==================== SUMMARY OF EVENTS FOR SUPPLIER #" +id+ " ====================");
        output("request  : " + requestCount);
        output("success  : " + successCount);
        output("rejected : " + rejectedCount);
        output("lost     : " + (requestCount.get() - successCount.get() - rejectedCount.get()));
    }

    public Integer getRequestCount() {
        return requestCount.get();
    }

    public Integer getSuccessCount() {
        return successCount.get();
    }

    public Integer getRejectedCount() {
        return rejectedCount.get();
    }
}
