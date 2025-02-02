package ca.uqac.performance;

/**
 * This simple config file holds all parameters needed to configure and balance the system.
 */
public class Config {

    /***************************************************
     * ############################################### *
     * #                THIS IS WHAT                 # *
     * #                   YOU'RE                    # *
     * #                LOOKING FOR                  # *
     * ############################################### *
     ***************************************************/
    // Full name for the system's adapter class
    public static final String SYSTEM_ADAPTER = "ca.uqac.performance.system.MyAdapterOptimized";
//    public static final String SYSTEM_ADAPTER = "ca.uqac.performance.system.MyAdapterDefault";

    /***************************************************
     * ############################################### *
     ***************************************************/


    /**
     * System defaults
     */
    public static final Boolean DEBUG_MODE = false;
    public static final Integer NUM_SUPPLIERS = 4;
    public static final Integer LOOP_INTERVAL = 2;
    public static final Integer NUM_LOOPS_TO_MAINTENANCE = 5000;
    public static final Integer IMBALANCE_THRESHOLD = 2;

    /**
     * This block defines costs constants.
     */
    public static final Integer COST_DEFAULT = 1;
    public static final Integer COST_IMBALANCE = 2;
    public static final Integer COST_OVERLOAD = 3;
    public static final Integer COST_REJECTION = 25;

    /**
     * This block defines requests sleep intervals.
     * Unit is microseconds (ms).
     */
    // The waiting time in between requests
    public static final Integer REQUEST_INTERVAL = 3;

    // The minimum time to process a request
    // Need to be less than REQUEST_PROCESS_TIME_MAX_MS
    public static final Integer REQUEST_PROCESS_TIME_MIN_MS = 1;

    // The maximum time to process a request
    // Need to be greater than REQUEST_PROCESS_TIME_MIN_MS
    public static final Integer REQUEST_PROCESS_TIME_MAX_MS = 3;

    /**
     * This block defines transformers parameters.
     */
    public static final Integer TRANSFORMER_BUFFER_OK = 3;
    public static final Integer TRANSFORMER_BUFFER_DANGER = 7;
    public static final Integer TRANSFORMER_BUFFER_MAX = 10;
}
