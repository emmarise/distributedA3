package part1;

import io.swagger.client.ApiClient;
import io.swagger.client.api.SkiersApi;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Multithreaded {
    //    private static final String PATH = "/ServletSkier_war_exploded"; // LOCAL
    private static final String PATH = "/ServletSkier"; // AWS
    private static final String HTTP_PREFIX = "http://";
    private static final double PHASE_PCT = 0.2;

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        // LOCAL --num_threads 64 --num_skiers 128 --num_lifts 40 --num_runs 20 --ip_address 152.44.141.6:8080
        // AWS   --num_threads 64 --num_skiers 128 --num_lifts 40 --num_runs 20 --ip_address 54.200.234.195:8080
        CommandParser parser = CommandParser.parseCommandArgs(args);
        int numThreads = parser.numThreads;
        int numSkiers = parser.numSkiers;
        int numLifts = parser.numLifts;
        int numRuns = parser.numRuns;
        String ipAddress = parser.ipAddress;

        // AWS   http://54.200.234.195:8080/ServletSkier_war
        String path = HTTP_PREFIX + ipAddress + PATH;
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(path);
        SkiersApi skiersApi = new SkiersApi(apiClient);

        int numThreadsPhase1 = numThreads / 4;
        int numThreadsPhase2 = numThreads;
        int numThreadsPhase3 =(int)Math.ceil(numThreads * 0.1);

        AtomicInteger numSuccessReq = new AtomicInteger(0);
        AtomicInteger numUnsuccessReq = new AtomicInteger(0);
        CountDownLatch totalCompleted = new CountDownLatch(numThreadsPhase1 + numThreadsPhase2 + numThreadsPhase3);

        int numPostsPhase1 = (int)Math.ceil((numRuns * 0.2) * (numSkiers / numThreadsPhase1));
        int numPostsPhase2 = (int)Math.ceil((numRuns * 0.6) * (numSkiers / numThreadsPhase2));
        int numPostsPhase3 = (int)Math.ceil(numRuns * 0.1 * numThreadsPhase3);

        // trigger phase 2 when 20% of phase 1 completed;
        int numTriggerPhase2 = (int)Math.ceil(numThreadsPhase1 * PHASE_PCT);
        CountDownLatch completedPhase2 = new CountDownLatch(numTriggerPhase2);
        // trigger phase 3 when 20% of phase 2 completed;
        int numTriggerPhase3 = (int)Math.ceil(numThreadsPhase2 * PHASE_PCT);
        CountDownLatch completedPhase3 = new CountDownLatch(numTriggerPhase3);

        int startTimePhase1 = 1;
        int endTimePhase1 = 90;
        int startTimePhase2 = 91;
        int endTimePhase2 = 360;
        int startTimePhase3 = 361;
        int endTimePhase3 = 420;

        Phase phase1 = new Phase(numThreadsPhase1, numSkiers, startTimePhase1, endTimePhase1,
                numLifts, numPostsPhase1, numSuccessReq, numUnsuccessReq, totalCompleted,
                completedPhase2, skiersApi);
        Phase phase2 = new Phase(numThreadsPhase2, numSkiers, startTimePhase2, endTimePhase2,
                numLifts, numPostsPhase2, numSuccessReq, numUnsuccessReq, totalCompleted,
                completedPhase3, skiersApi);
        Phase phase3 = new Phase(numThreadsPhase3, numSkiers, startTimePhase3, endTimePhase3,
                numLifts, numPostsPhase3, numSuccessReq, numUnsuccessReq, totalCompleted,
                completedPhase3, skiersApi);


        // run phase
        long start = System.currentTimeMillis();

        System.out.println("------  PHASE 1  ------");
        phase1.start();
        completedPhase2.await();

        System.out.println("------  PHASE 2  ------");
        phase2.start();
        completedPhase3.await();

        System.out.println("------  PHASE 3  ------");
        phase3.start();
        totalCompleted.await();

        long end = System.currentTimeMillis();

        long duration = end - start;
        long throughput = (numSuccessReq.get() + numUnsuccessReq.get()) / duration * 1000;
        System.out.println("\n------  Statistics  ------\n");
        System.out.println("------  PART 1  ------");
        System.out.println("number of successful requests sent: " + numSuccessReq);
        System.out.println("number of unsuccessful requests: " + numUnsuccessReq);
        System.out.println("the total run time for all phases to complete: " + duration);
        System.out.println("the total throughput in requests per second: " + throughput);
        System.out.println();

        List<String[]> records = new ArrayList<>();
        records.addAll(phase1.records);
        records.addAll(phase2.records);
        records.addAll(phase3.records);

        System.out.println("------  PART 2  ------");
        Calculator calculator = new Calculator(records, start, end);
        System.out.println("mean response time (millisecs): " + calculator.getMeanResponse());
        System.out.println("median response time (millisecs): " + calculator.getMedianResponse());
        System.out.println("throughput: " + calculator.getThroughput());
        System.out.println("p99 (99th percentile) response time: " + calculator.getP99Response());
        System.out.println("min response time (millisecs): " + calculator.getMinResponse());
        System.out.println("max response time (millisecs): " + calculator.getMaxResponse());
    }
}
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.api.SkiersApi;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.atomic.AtomicInteger;
//
//public class Multithreaded {
//
////      private static final String WEB_PACKAGE = "/ServletSkier_war_exploded";  // local
//    private static final String WEB_PACKAGE = "/ServletSkier";  // aws
//
//    public static void main(String[] args) throws ApiException, InterruptedException {
//        // args[]:
//        // --numThreads 1024 --numSkier 100 --numLifts 10 --numRuns 15 --ipPort 18.237.217.26:8080
//        // --numThreads 1024 --numSkier 100 --numLifts 10 --numRuns 15 --ipPort localhost:8080
//        CommandParser commandParser = CommandParser.parseArgs(args);
//
//        ApiClient apiClient = new ApiClient();
//        // Path example: "http://localhost:8080/ServletSkier_war_exploded"
//        apiClient.setBasePath("http://" + commandParser.ipPort + WEB_PACKAGE);
//        SkiersApi skiersApi = new SkiersApi(apiClient);
//
//        // Get parameters from command line
//        int numThreads = commandParser.numThreads;
//        int numSkiers = commandParser.numSkiers;
//        int numLifts = commandParser.numLifts;
//        int numRuns = commandParser.numRuns;
//        int numPostP1 =  (int)(numRuns * 0.2) * (numSkiers / (numThreads / 4));
//        int numPostP2 =  (int)(numRuns * 0.6) * (numSkiers / numThreads);
//        int numPostP3 =  (int)(numRuns * 0.1) * (numSkiers / (numThreads / 10));
//
//        int numThreadsP1 = numThreads / 4;
//        int numThreadsP2 = numThreads;
//        int numThreadsP3 = numThreads / 10;
//        CountDownLatch totalCount = new CountDownLatch(numThreadsP1 + numThreadsP2 + numThreadsP3);
//
//        AtomicInteger successCalls = new AtomicInteger(0);
//        AtomicInteger failCalls = new AtomicInteger(0);
//
//        // ******* part1.Phase 1 *******
//        int P2Trigger = (int)Math.ceil(numThreadsP1 / 20.0);
//        int startTimeP1 = 1;
//        int endTimeP1 = 90;
//        CountDownLatch triggerCountP2 = new CountDownLatch(P2Trigger);
//        Phase phase1 = new Phase(numThreads, numSkiers, numLifts, startTimeP1,
//                endTimeP1, numPostP1, successCalls, failCalls, totalCount, triggerCountP2, skiersApi);
//
//        // ******* part1.Phase 2 *******
//        int P3Trigger = (int)Math.ceil(numThreadsP2 / 20.0);
//        int startTimeP2 = 91;
//        int endTimeP2 = 360;
//        CountDownLatch triggerCountP3 = new CountDownLatch(P3Trigger);
//        Phase phase2 = new Phase(numThreads, numSkiers, numLifts, startTimeP2,
//                endTimeP2, numPostP2, successCalls, failCalls, totalCount, triggerCountP3, skiersApi);
//
//        // ******* part1.Phase 3 *******
//        int startTimeP3 = 361;
//        int endTimeP3 = 420;
//        Phase phase3 = new Phase(numThreads, numSkiers, numLifts, startTimeP3,
//                endTimeP3, numPostP3, successCalls, failCalls, totalCount, triggerCountP3, skiersApi);
//
//        // part1. Main logic for running three phases
//        long startTime = System.currentTimeMillis();
//        System.out.println("-----------PHASE1-----------");
//        phase1.start();
//        triggerCountP2.await();
//        System.out.println("-----------PHASE2-----------");
//        phase2.start();
//        triggerCountP3.await();
//        System.out.println("-----------PHASE3-----------");
//        phase3.start();
//        totalCount.await();
//        long endTime = System.currentTimeMillis();
//        long totalRunTime = endTime - startTime;
//        System.out.println("Number of successful requests sent: " + successCalls);
//        System.out.println("Number of unsuccessful request: " + failCalls);
//        System.out.println("The total run time for all phases to complete: " + totalRunTime);
//        System.out.println("Throughput: " + ((successCalls.get() + failCalls.get()) / (totalRunTime/ 1000)));
//    }
//}