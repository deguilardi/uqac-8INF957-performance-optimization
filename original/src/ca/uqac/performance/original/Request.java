package ca.uqac.performance.original;

import static ca.uqac.performance.original.Config.REQUEST_PROCESS_TIME_MAX_MS;
import static ca.uqac.performance.original.Config.REQUEST_PROCESS_TIME_MIN_MS;
import static ca.uqac.performance.original.Debug.debug;

public class Request {
    private long processingTime = (long) (Math.random() * (REQUEST_PROCESS_TIME_MAX_MS - REQUEST_PROCESS_TIME_MIN_MS + 1) + REQUEST_PROCESS_TIME_MIN_MS);

    Request(){
        debug("Creating request with processingTime: " + processingTime);
    }

    public long getProcessingTime() {
        return processingTime;
    }
}
