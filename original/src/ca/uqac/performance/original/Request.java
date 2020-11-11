package ca.uqac.performance.original;

import com.sun.org.apache.xpath.internal.operations.Bool;

import static ca.uqac.performance.original.Config.*;
import static ca.uqac.performance.original.Debug.debug;

public class Request {
    private long processingTime = (long) (Math.random() * (REQUEST_PROCESS_TIME_MAX_MS - REQUEST_PROCESS_TIME_MIN_MS + 1) + REQUEST_PROCESS_TIME_MIN_MS);
    private Boolean success;
    private Boolean unbalanced = false;
    private Integer cost = COST_DEFAULT;

    Request(){
        debug("Creating request with processingTime: " + processingTime);
    }

    public void respond(){
        success = true;
    }

    public void reject(){
        success = false;
        cost = COST_REJECTION;
        debug("Request rejected, with cost: " + cost);
    }

    public Integer getCost(){
        return cost;
    }

    public void unbalance(){
        unbalanced = true;
        cost = COST_IMBALANCE;
        debug("Request balanced, with cost: " + cost);
    }

    public Boolean isSuccessful() {
        return success != null && success == true;
    }

    public Boolean isUnbalanced() {
        return unbalanced;
    }

    public boolean isLost() {
        return success == null;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    @Override
    public String toString() {
        if(success == null){
            return "Request lost";
        } else if(success == true) {
            return "Request processed successfully, took " + getProcessingTime() + "ms";
        } else {
            return "Request rejected";
        }
    }
}
