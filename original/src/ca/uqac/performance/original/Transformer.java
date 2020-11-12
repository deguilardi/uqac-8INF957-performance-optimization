package ca.uqac.performance.original;

import ca.uqac.performance.original.util.Sleeper;

import java.util.LinkedList;
import java.util.Queue;

import static ca.uqac.performance.original.Config.*;
import static ca.uqac.performance.original.util.Debug.debug;

public class Transformer {

    public enum State{
        IDLE,
        OK,
        DANGER,
        MAX
    }

    private Integer id;
    private Queue<Request> buffer = new LinkedList<>();
    private State state = State.IDLE;
    private Boolean isProcessing = false;

    public Transformer(int id) {
        this.id = id;
    }

    public void pushRequest(Request request, Supplier fromSupplier) {
        if(state.equals(State.MAX)) {
            debug("Buffer is full.");
            request.reject();
            fromSupplier.receiveResponseFor(request);
            return;
        }
        buffer.add(request);
        checkState();
    }

    public Request pollRequest(){
        return buffer.poll();
    }

    public Request peekRequest(){
        return buffer.peek();
    }

    public void processFor(Supplier supplier) {
        if(isProcessing){
            debug("Transformer busy.");
            return;
        }

        Request headRequest = pollRequest();
        if(headRequest == null){
            debug("Transformer buffer is empty.");
            return;
        }

        isProcessing = true;
        Processor processor = new Processor(this, headRequest, supplier);
        processor.start();
    }

    public Integer getId() {
        return id;
    }

    public Integer getLoad(){
        return buffer.size();
    }

    private void checkState(){
        if (buffer.size() >= TRANSFORMER_BUFFER_MAX){
            state = State.MAX;
        } else if (buffer.size() >= TRANSFORMER_BUFFER_DANGER){
            state = State.DANGER;
        } else if (buffer.size() >= TRANSFORMER_BUFFER_OK){
            state = State.OK;
        } else {
            state = State.IDLE;
        }
    }

    public State getState() {
        return state;
    }

    private class Processor extends Thread{
        Transformer transformer;
        Request request;
        Supplier supplier;

        Processor(Transformer transformer, Request request, Supplier supplier){
            this.request = request;
            this.supplier = supplier;
            this.transformer = transformer;
        }

        @Override
        public void run() {
            super.run();
            debug("started processing.");
            Sleeper.unsafeSleep(request.getProcessingTime());
            request.respond();
            supplier.receiveResponseFor(request);
            checkState();
            isProcessing = false;
            debug("finished processing.");
        }
    }

}
