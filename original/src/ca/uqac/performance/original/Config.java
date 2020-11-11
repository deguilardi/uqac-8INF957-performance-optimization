package ca.uqac.performance.original;

public class Config {
    public static final Boolean DEBUG_MODE = true;
    public static final Boolean OPTIMIZED_MODE = true;

    public static final Integer NUM_SUPPLIERS = 4;
    public static final Integer LOOP_INTERVAL = 2;
    public static final Integer NUM_LOOPS_TO_MAINTENANCE = 50; // 5000

    public static final Integer REQUEST_INTERVAL = 3;
    public static final Integer REQUEST_PROCESS_TIME_MIN_MS = 1;
    public static final Integer REQUEST_PROCESS_TIME_MAX_MS = 3;

    public static final Integer TRANSFORMER_BUFFER_OK = 3;
    public static final Integer TRANSFORMER_BUFFER_DANGER = 7;
    public static final Integer TRANSFORMER_BUFFER_MAX = 10;
}
