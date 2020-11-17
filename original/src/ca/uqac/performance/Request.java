package ca.uqac.performance;

import ca.uqac.performance.util.Debug;

/**
 * Request objects send from suppliers to transformers.
 * The same object us sent back from transformers to suppliers when request is responded.
 */
public class Request {
    private long processingTime = (long) (Math.random() * (Config.REQUEST_PROCESS_TIME_MAX_MS - Config.REQUEST_PROCESS_TIME_MIN_MS + 1) + Config.REQUEST_PROCESS_TIME_MIN_MS);
    private Boolean success;
    private Boolean unbalanced = false;
    private Boolean overloaded = false;
    private Integer cost = Config.COST_DEFAULT;

    Request(){
        Debug.debug("Creating request with processingTime: " + processingTime);
    }

    public void respond(){
        success = true;
    }

    public void reject(){
        success = false;
        cost = Config.COST_REJECTION;
        Debug.debug("Request rejected, with cost: " + cost);
    }

    public Integer getCost(){
        return cost;
    }

    public void unbalance(){
        if(!unbalanced) {
            unbalanced = true;
            cost += Config.COST_IMBALANCE;
            Debug.debug("Request balanced, with cost: " + cost);
        }
    }

    public void overload() {
        overloaded = true;
        cost += Config.COST_OVERLOAD;
    }

    public Boolean isSuccessful() {
        return success != null && success == true;
    }

    public Boolean isUnbalanced() {
        return unbalanced;
    }

    public boolean isOverloaded() {
        return overloaded;
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
