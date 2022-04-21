package part1;

public class CommandParser {
    public int numThreads;
    public int numSkiers;
    public int numLifts;
    public int numRuns;
    public String ipAddress;

    public static final String ARG_PREFIX = "--";
    public static final String NUM_THREADS = "numThreads";
    public static final String NUM_SKIERS = "numSkier";
    public static final String NUM_LIFTS = "numLifts";
    public static final String NUM_RUNS = "numRuns";
    public static final String IP_ADDRESS = "ipPort";

    public static final int SKI_DAY = 420;
    public static final int MAX_NUM_THREADS = 1024;
    public static final int MAX_NUM_SKIERS = 100000;
    public static final int DEFAULT_NUM_LIFTS = 40;
    public static final int MIN_NUM_LIFTS = 5;
    public static final int MAX_NUM_LIFTS = 60;
    public static final int DEFAULT_NUM_RUNS = 10;
    public static final int MAX_NUM_RUNS = 20;

    public CommandParser(int numThreads, int numSkiers, int numLifts, int numRuns, String ipAddress) {
        this.numThreads = numThreads;
        this.numSkiers = numSkiers;
        this.numLifts = numLifts;
        this.numRuns = numRuns;
        this.ipAddress = ipAddress;
    }

    public static CommandParser parseCommandArgs(String[] args) {
        int numThreads = -1;
        int numSkiers = -1;
        int numLifts = DEFAULT_NUM_LIFTS;
        int numRuns = DEFAULT_NUM_RUNS;
        String ipAddress = null;

        int index = 0;
        for (int i = 0; i < args.length; i += 2) {
            String paramStr = args[i];
            if (!paramStr.startsWith(ARG_PREFIX)) {
                throw new IllegalArgumentException("Incorrect argument prefix, use --flag instead.");
            }
            String param = paramStr.substring(2);
            String valStr = args[i+1];
            switch(param) {
                case NUM_THREADS:
                    numThreads = Integer.parseInt(valStr);
                    if (numThreads > MAX_NUM_THREADS) {
                        throw new IllegalArgumentException(String.format("Invalid number of threads to run, max to %d.", MAX_NUM_THREADS));
                    }
                    break;
                case NUM_SKIERS:
                    numSkiers = Integer.parseInt(valStr);
                    if (numSkiers > MAX_NUM_SKIERS) {
                        throw new IllegalArgumentException(String.format("Invalid number of skiers, max to %d.", MAX_NUM_SKIERS));
                    }
                    break;
                case NUM_LIFTS:
                    numLifts = Integer.parseInt(valStr);
                    if (numLifts < MIN_NUM_LIFTS || numLifts > MAX_NUM_LIFTS) {
                        throw new IllegalArgumentException(String.format("Invalid number of lifts, range from %d to %d.", MIN_NUM_LIFTS, MAX_NUM_LIFTS));
                    }
                    break;
                case NUM_RUNS:
                    numRuns = Integer.parseInt(valStr);
                    if (numRuns > MAX_NUM_RUNS) {
                        throw new IllegalArgumentException(String.format("Invalid mean number of lifts per skier rides, max to %d.", MAX_NUM_RUNS));
                    }
                    break;
                case IP_ADDRESS:
                    ipAddress = valStr;
                    if (valStr.isEmpty()) {
                        throw new IllegalArgumentException("Empty IP address.");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect argument, use the available flag instead.");
            }
        }
        if (numThreads == -1 || numSkiers == -1 || ipAddress == null) {
            throw new IllegalArgumentException("Missing arguments.");
        }
        return new CommandParser(numThreads, numSkiers, numLifts, numRuns, ipAddress);
    }
}

//public class CommandParser {
//    // --numThreads 100 --numSkier 100 --numLifts 10 --numRuns 15 --IpPort 192.168.1.67:8080
//    public int numThreads;
//    public int numSkiers;
//    public int numLifts;
//    public int numRuns;
//    public String ipPort;
//
//    public CommandParser(int numThreads, int numSkiers, int numLifts, int numRuns, String ipPort) {
//        this.numThreads = numThreads;
//        this.numSkiers = numSkiers;
//        this.numLifts = numLifts;
//        this.numRuns = numRuns;
//        this.ipPort = ipPort;
//    }
//
//    public static CommandParser parseArgs(String[] args) {
//        int numThreadsTemp = 0;
//        int numSkiersTemp = 50000; // max = 100000
//        int numLiftsTemp = 40; // default = 40
//        int numRunsTemp = 10; // default = 10
//        String ipPortTemp = null;
//
//        // --numThreads 100 --numSkiers 100 --numLifts 10 --numRuns 15 --ipPort 192.168.1.67:8080
//        for (int i = 0; i < args.length; i++) {
//            String option = args[i];
//            String value = args[++i];
//            switch (option) {
//                case "--numThreads":
//                    numThreadsTemp = Integer.parseInt(value);
//                    break;
//                case "--numSkiers":
//                    numSkiersTemp = Integer.parseInt(value);
//                    break;
//                case "--numLifts":
//                    numLiftsTemp = Integer.parseInt(value);
//                    break;
//                case "--numRuns":
//                    numRunsTemp = Integer.parseInt(value);
//                    break;
//                case "--ipPort":
//                    ipPortTemp = value;
//                    break;
//            }
//        }
//
//        return new CommandParser(numThreadsTemp, numSkiersTemp, numLiftsTemp, numRunsTemp, ipPortTemp);
//    }
//}
