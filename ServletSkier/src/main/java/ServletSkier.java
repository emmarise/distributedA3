import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@WebServlet(name = "ServletSkier", value = "/skiers/*")
public class ServletSkier extends HttpServlet {
    private String RESORTS = "resorts";
    private String SEASONS = "seasons";
    private String DAY = "day";
    private String DAYS = "days";
    private String SKIERS = "skiers";
    private String VERTICAL = "vertical";
    private String STATISTICS = "statistics";
    private String SERVER_QUEUE = "skier_queue";
    private ObjectPool<Channel> pool;
    private static final String EXCHANGE_NAME = "logs";
    private static final String EXCHANGE_TYPE = "fanout";

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            pool = new GenericObjectPool<>(new ChannelFactory());
        } catch (IOException e) {
            System.out.println("ERROR: Fail to initialize.");
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("ERROR: Fail to initialize.");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        String urlPath = req.getPathInfo();
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("URL is null or empty");
            return;
        }
        // urlPath = "/a/b/c"
        // urlParts = ["", "a", "b", "c"]
        String[] urlParts = urlPath.split("/");
        int urlPartsSize = urlParts.length;

        // resorts GET: get a list of ski resorts in the database
        //  0    1
        // ["", "resorts"]
        if (urlPartsSize == 2 && urlParts[1].equalsIgnoreCase(RESORTS)) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("{\n" +
                    "  \"resorts\": [\n" +
                    "    {\n" +
                    "      \"resortName\": \"Creek Lake\",\n" +
                    "      \"resortID\": 0\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}");
        }
        // resorts GET: get number of unique skiers at resort/season/day
        //  0    1         2           3       4           5      6       7
        // ["", "resorts", resortID, "seasons", seasonID, "day", dayID, "skiers"]
        else if (urlPartsSize == 8 &&
                urlParts[1].equalsIgnoreCase(RESORTS) &&
                urlParts[3].equalsIgnoreCase(SEASONS) &&
                urlParts[5].equalsIgnoreCase(DAY) &&
                urlParts[7].equalsIgnoreCase(SKIERS)) {
            String resortId = urlParts[2];
            String seasonID = urlParts[4];
            String dayID = urlParts[6];
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("{\n" +
                    "  \"time\": \"Mission Ridge\",\n" +
                    "  \"numSkiers\": 78999\n" +
                    "}");
        }
        // resorts GET: get a list of seasons for the specified resort
        //  0    1         2           3
        // ["", "resorts", resortID, "seasons"]
        else if (urlPartsSize == 4 &&
                urlParts[1].equalsIgnoreCase(RESORTS) &&
                urlParts[3].equalsIgnoreCase(SEASONS)) {
            String resortId = urlParts[2];
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("{\n" +
                    "  \"seasons\": [\n" +
                    "    \"winter\"\n" +
                    "  ]\n" +
                    "}");
        }
        // skiers GET: get the total vertical for the skier for the specified ski day
        //  0    1         2           3       4           5      6       7        8
        // ["", "skiers", resortID, "seasons", seasonID, "days", dayID, "skiers", skierID]
        else if (urlPartsSize == 9 &&
                urlParts[1].equalsIgnoreCase(SKIERS) &&
                urlParts[3].equalsIgnoreCase(SEASONS) &&
                urlParts[5].equalsIgnoreCase(DAYS) &&
                urlParts[7].equalsIgnoreCase(SKIERS)) {
            String resortId = urlParts[2];
            String seasonID = urlParts[4];
            String dayID = urlParts[6];
            String skierID = urlParts[8];
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("34507");
        }
        // skiers GET: get the total vertical for the skier the specified resort. If no season is specified, return all seasons
        //  0    1         2           3
        // ["", "skiers", skierID, "vertical"]
        else if (urlPartsSize == 4 && urlParts[1].equalsIgnoreCase(SKIERS) && urlParts[3].equalsIgnoreCase(VERTICAL)) {
            String skierID = urlParts[2];
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("{\n" +
                    "  \"resorts\": [\n" +
                    "    {\n" +
                    "      \"seasonID\": \"Creek Lake\",\n" +
                    "      \"totalVert\": 100\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}");
        }
        // skiers GET: get the API performance stats
        //  0    1
        // ["", "statistics"]
        else if (urlPartsSize == 2 && urlParts[1].equalsIgnoreCase(STATISTICS)) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("{\n" +
                    "  \"endpointStats\": [\n" +
                    "    {\n" +
                    "      \"URL\": \"/resorts\",\n" +
                    "      \"operation\": \"GET\",\n" +
                    "      \"mean\": 11,\n" +
                    "      \"max\": 198\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}");
        } else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("URL format or parameters are invalid");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        System.out.println("doPost");
        res.setContentType("application/json");
        String urlPath = req.getPathInfo();
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            System.out.println("Not found");
//            res.getWriter().write("URL is null or empty");
            return;
        }
        // urlPath = "/a/b/c"
        // urlParts = ["", "a", "b", "c"]
        String[] urlParts = urlPath.split("/");
        int urlPartsSize = urlParts.length;
//        System.out.println(urlPath);
//        System.out.println(urlParts);
//        System.out.println(urlPartsSize);
        // resorts POST: Add a new season for a resort
        //  0      1          2          3
        // ["", resortID, "seasons"]
        if (urlPartsSize == 3 && urlParts[2].equalsIgnoreCase(SEASONS)) {
            String resortId = urlParts[1];
            try {
                String postBodyStr = req.getReader().lines().collect(Collectors.joining());
                JsonObject postBodyJson = new JsonParser().parse(postBodyStr).getAsJsonObject();
                postBodyJson.addProperty("resortId", resortId);
                postBodyJson.addProperty("type", "resorts");
                sendDataToQueue(postBodyJson);
                res.setStatus(HttpServletResponse.SC_CREATED);
                res.getWriter().write("new season created");
            } catch (Exception e) {
                e.printStackTrace();
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("POST body is invalid");
            }
        }
        // skiers POST: write a new lift ride for the skier
        //  0    1         2           3       4           5      6       7
        // ["", resortID, "seasons", seasonID, "days", dayID, "skiers", skierID]
        else if (urlPartsSize == 8 &&
//                urlParts[1].equalsIgnoreCase(SKIERS) &&
                urlParts[2].equalsIgnoreCase(SEASONS) &&
                urlParts[4].equalsIgnoreCase(DAYS) &&
                urlParts[6].equalsIgnoreCase(SKIERS)) {
//            System.out.println("else");
            String resortId = urlParts[1];
            String seasonID = urlParts[3];
            String dayID = urlParts[5];
            String skierID = urlParts[7];
            try {
                String postBodyStr = req.getReader().lines().collect(Collectors.joining());
                JsonObject postBodyJson = new JsonParser().parse(postBodyStr).getAsJsonObject();
                postBodyJson.addProperty("resortId", resortId);
                postBodyJson.addProperty("seasonID", seasonID);
                postBodyJson.addProperty("dayID", dayID);
                postBodyJson.addProperty("skierID", skierID);
                postBodyJson.addProperty("type", "lift_ride");
                sendDataToQueue(postBodyJson);
                res.setStatus(HttpServletResponse.SC_CREATED);
//                res.getWriter().write("Write successful");
            } catch (Exception e) {
                e.printStackTrace();
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                res.getWriter().write("POST body is invalid");
            }
        } else {
//            System.out.println("last place");
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
//            res.getWriter().write("URL format or parameters are invalid");
        }
    }

    private boolean sendDataToQueue(JsonObject bodyJson) {
        try {
            System.out.println("sendData");
            Channel channel = pool.borrowObject();
            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
//             channel.queueDeclare(SERVER_QUEUE, true, false, false, null);
            channel.basicPublish(EXCHANGE_NAME, SERVER_QUEUE, null, bodyJson.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("Successful!");
            pool.returnObject(channel);
//             System.out.println(String.format(" [x] Sent '%s'", bodyJson));
            return true;
        } catch (IOException e) {
            System.out.println("ERROR: Fail to send data to queue.");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("ERROR: Fail to send data to queue.");
            e.printStackTrace();
            return false;
        }
    }
}
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.ConnectionFactory;
//import org.apache.commons.pool2.ObjectPool;
//import org.apache.commons.pool2.impl.GenericObjectPool;
//
//import java.util.concurrent.BlockingDeque;
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//import java.util.stream.Collectors;
//import javax.servlet.ServletException;
//import javax.servlet.http.*;
//import javax.servlet.annotation.*;
//import java.io.IOException;
//
//@WebServlet(name = "ServletSkier")
//public class ServletSkier extends HttpServlet {
//    // RMQ's ip address
//    private static String ipAddress = "54.189.11.152";
//    private static String userName = "furong";
//    private static String passwd = "123456";
//    int CHANNELS = 128;
//    BlockingDeque<Channel> blockingDeque;
//    public void init(){
//        blockingDeque = new LinkedBlockingDeque<>();
//        ConnectionFactory connectionFactory = new ConnectionFactory();
//        connectionFactory.setHost(ipAddress);
//        connectionFactory.setUsername(userName);
//        connectionFactory.setPassword(passwd);
////        connectionFactory.setVirtualHost("/");
//        Connection connection = null;
//        try {
//            connection = connectionFactory.newConnection();
//            for(int i = 0; i < CHANNELS; i++){
//                blockingDeque.add(connection.createChannel());
//            }
//        } catch (TimeoutException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//    private static final int DAY_MIN = 1;
//    private static final int DAY_MAX = 366;
//    private static final String SKIERS = "skiers";
//    private static final String SEASONS = "seasons";
//    private static final String DAYS = "days";
//
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        response.setContentType("application/json");
//        String urlPath = request.getPathInfo();
//
//        // check we have a URL!
//        if (urlPath == null || urlPath.isEmpty()) {
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//            response.getWriter().write("invalid url");
//            return;
//        }
//
//        String[] urlParts = urlPath.split("/");
//        int urlPartsSize = urlParts.length;
//        // and now validate url path and return the response status code
//        // (and maybe also some value if input is valid)
//
//        if (!isUrlValid(urlParts)) {
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//        } else {
////            response.setStatus(HttpServletResponse.SC_OK);
////        http://localhost:8080/ServletSkier_war_exploded/skiers/1997/seasons/9977/days/114/skiers/1114
//            String bodyJson = request.getReader().lines().collect(Collectors.joining());
//            JsonObject obj = new JsonParser().parse(bodyJson).getAsJsonObject();
//            obj.addProperty("skierId", urlParts[7]);
//            // urlParts = [, 1, seasons, 2019, days, 1, skiers, 123]
//            // /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
//            try {
//                Channel channel = blockingDeque.take();
//                channel.queueDeclare("skier_queue", false, false, false, null);
//                channel.basicPublish("", "skier_queue", null, obj.toString().getBytes());
//                blockingDeque.add(channel);
//
//                response.setStatus(HttpServletResponse.SC_CREATED);
////                response.getWriter().write("Success: " + urlPath + " " + liftRide.toString());
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }
//
//    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
//        res.setContentType("application/json");
//        String urlPath = req.getPathInfo();
//
//        // check we have a URL!
//        if (urlPath == null || urlPath.isEmpty()) {
//            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
//            res.getWriter().write("invalid url");
//            return;
//        }
//
//        String[] urlParts = urlPath.split("/");
//        // and now validate url path and return the response status code
//        // (and maybe also some value if input is valid)
//
//        if (!isUrlValid(urlParts)) {
//            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
//        } else {
//            res.setStatus(HttpServletResponse.SC_OK);
//            // do any sophisticated processing with urlParts which contains all the url params
//            res.getWriter().write("{\n" +
//                            "  \"skiers\": " + urlParts[7] +
//                    "}");
//        }
//    }
//
//    private boolean isUrlValid(String[] urlPath) {
////        return true;
////        http://localhost:8080/ServletSkier_war_exploded/skiers/1997/seasons/9977/days/114/skiers/1114
//        if(urlPath.length == 8) {
//            return isNumeric(urlPath[1]) &&
//                    urlPath[2].equals(SEASONS) &&
//                    isNumeric(urlPath[3]) &&
//                    urlPath[3].length() == 4 &&
//                    urlPath[4].equals(DAYS) &&
//                    isNumeric(urlPath[5]) &&
//                    Integer.parseInt(urlPath[5]) >= DAY_MIN &&
//                    Integer.parseInt(urlPath[5]) <= DAY_MAX &&
//                    urlPath[6].equals(SKIERS) &&
//                    isNumeric(urlPath[7]);
//        }
//        return false;
//    }
//
//    private boolean isNumeric(String s) {
//        if(s == null || s.equals("")) return false;
//        try {
//            Integer.parseInt(s);
//            return true;
//        } catch (NumberFormatException ignored) { }
//        return false;
//    }
//}
