package ca.uqac.performance.original;

public class Response {

    private Boolean success;
    private Transformer transformer;
    private Request request;

    private Response(Boolean success, Transformer transformer, Request request) {
        this.success = success;
        this.transformer = transformer;
        this.request = request;
    }

    public static Response factoryRejected(Transformer transformer, Request request){
        return new Response(false, transformer, request);
    }

    public static Response factorySuccess(Transformer transformer, Request request){
        return new Response(true, transformer, request);
    }

    public Boolean isSuccessful() {
        return success;
    }

    @Override
    public String toString() {
        if(success) {
            return "Request processed by transformer #" + transformer.getId() + ", took " + request.getProcessingTime() + "ms";
        } else {
            return "Request rejected by transformer #" + transformer.getId();
        }
    }
}
