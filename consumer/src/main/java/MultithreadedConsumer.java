import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import redis.clients.jedis.JedisPooled;

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.concurrent.TimeoutException;

public class MultithreadedConsumer {
  private static String REDIS_HOST = "54.212.137.104";
  private static String RMQ_HOST = "35.88.89.249";
  private static String USERNAME = "furong";
  private static String PASSWORD = "123456";
  private static String VHOST = "/";
  private static String QUEUE_NAME = "skier_queue";

  private static int MAX_THREAD = 128;
  private static String LIFT_RIDE = "lift_ride";
  private static final String EXCHANGE_NAME = "logs";
  private static final String EXCHANGE_TYPE = "fanout";
  private static SkierDao skierDao;

  public static void main(String args[]) throws IOException, TimeoutException {
    JedisPooled jedis = new JedisPooled(REDIS_HOST, 6379);
    skierDao = new SkierDao(jedis);

    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(RMQ_HOST);
    connectionFactory.setUsername(USERNAME);
    connectionFactory.setPassword(PASSWORD);
    connectionFactory.setVirtualHost(VHOST);
    Connection connection = connectionFactory.newConnection();

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME, true, false, false, null);
          channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
          channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
//          System.out.println("[*] Waiting for messages. To exit press CTRL+C");
          DeliverCallback deliverCallback = (tag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
//             System.out.println(" [x] Received '" + message + "'");
            createSkier(message);
//            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          };
          channel.basicConsume(QUEUE_NAME, true, deliverCallback, tag -> {});
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };
    for (int i = 0; i < MAX_THREAD; i++) {
      Thread thread = new Thread(runnable);
      thread.start();
    }
  }

  private synchronized static void createSkier(String msg) throws InvalidPropertiesFormatException {
    Gson gson = new Gson();
    Message message = gson.fromJson(msg, Message.class);
    Integer liftTime = message.getTime();
    Integer liftId = message.getLiftID();
    String resortId = message.getResortId();
    String skierId = message.getSkierID();
    String seasonId = message.getSeasonID();
    String type = message.getType();
    String dayId = message.getDayID();

    if (type.equals(LIFT_RIDE)) {
      LiftRide liftRide = new LiftRide(liftId, liftTime, seasonId, dayId, skierId, resortId);
      skierDao.createSkier(liftRide);
    } else {
      throw new InvalidPropertiesFormatException("The post message format is not valid.");
    }
  }
}

//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.ConnectionFactory;
//import com.rabbitmq.client.DeliverCallback;
//import redis.clients.jedis.*;
//
//
//import java.io.IOException;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.TimeoutException;
//
//public class MultithreadedConsumer {
//
//  private static final String QUEUE_NAME = "skier_queue";
//  private static final int NO_OF_MSG_PER_RECEIVER = 1;
//  private static ConcurrentMap<Integer, List<LiftRide>> skierDataMap = new ConcurrentHashMap<>();
//  private static JedisPool pool;
//
//  public static void main(String args[]) throws IOException, TimeoutException {
////    pool = new JedisPool("34.211.224.177", 6379);
////    try (Jedis jedis = pool.getResource()) {
////      jedis.set("a", "2");
////    }
//
//    pool = new JedisPool("54.202.117.67", 6379);
//    ConsumerParameters parameters = ParameterProcessor.processParameters();
//    ConnectionFactory factory = new ConnectionFactory();
//    factory.setHost(parameters.getHostName());
//    factory.setUsername(parameters.getUserName());
//    factory.setPassword(parameters.getPassword());
////    factory.setVirtualHost("/");
//    Connection connection = factory.newConnection();
//    Runnable runnable = new Runnable() {
//      @Override
//      public void run() {
//        try {
//          Channel channel = connection.createChannel();
//          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//          channel.basicQos(NO_OF_MSG_PER_RECEIVER);
//          System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
//          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//            String message = new String(delivery.getBody(), "UTF-8");
//            //true for acknowledging multiple deliveries false otherwise Message gets deleted
//            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
////            System.out.println("Message received:" + message);
//            updateSkierDataInMap(message);
//          };
//          boolean autoAck = false;
//          channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> {
//          });
//        } catch (IOException e) {
////          e.printStackTrace();
//        }
//      }
//    };
//    for (int i = 0; i < parameters.getMaxThreads(); ++i) {
//      Thread newThread = new Thread(runnable);
//      newThread.start();
//    }
//  }
//  // http://18.237.217.26:8080/ServletSkier_war/skiers/1997/seasons/1997/days/0114/skiers/860
//  // resort, season, days, skiersId, body:{time, liftIdm, waitId}
//  // message: 1997,1997,114,860,217,21,3
//  private synchronized static void updateSkierDataInMap(String message) {
//    Gson gson = new Gson();
//    SkierInfo skierInfo = gson.fromJson(message, SkierInfo.class);
//////    System.out.println(
//////        "SkierInfo: skierId:" + skierInfo.getSkierId() + " LiftrideInfo: time:" + skierInfo.getTime() + " liftId:"
//////            + skierInfo.getLiftId() + "waitTime:" + skierInfo.getWaitTime());
//////    LiftRide liftRide = new LiftRide(skierInfo.getTime(), skierInfo.getLiftId(), skierInfo.getWaitTime());
//
////    JedisPool pool = new JedisPool("54.202.206.163", 6379);
//    UUID uuid = UUID.randomUUID();
//    try (Jedis jedis = pool.getResource()) {
//      jedis.set(uuid.toString(), message);
////      jedis.set(String.valueOf(skierInfo.getSkierId()),
////              jedis.get(String.valueOf(skierInfo.getSkierId())) + message);
//    }
//
////    if (skierDataMap.containsKey(skierInfo.getSkierId())) {
////      List<LiftRide> liftRideList = skierDataMap.get(skierInfo.getSkierId());
////      liftRideList.add(new LiftRide(skierInfo.getTime(), skierInfo.getLiftId(), skierInfo.getWaitTime()));
////    } else {
////      List<LiftRide> newLiftRideList = new ArrayList<>();
////      newLiftRideList.add(liftRide);
////      skierDataMap.put(skierInfo.getSkierId(), newLiftRideList);
////    }
////    System.out.println("Map size: " + skierDataMap.size());
//  }
//}