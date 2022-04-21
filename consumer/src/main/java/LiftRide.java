import com.google.gson.annotations.SerializedName;
public class LiftRide {
    private Integer liftID;
    private Integer liftTime;
    private String seasonID;
    private String dayID;
    private String skierID;
    private String resortID;
    private Integer vertical;

    public LiftRide(Integer liftID, Integer liftTime, String seasonID, String dayID, String skierID, String resortID) {
        this.liftID = liftID;
        this.liftTime = liftTime;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.skierID = skierID;
        this.resortID = resortID;
        this.vertical = this.liftID * 10;
    }

    public Integer getLiftID() {
        return liftID;
    }

    public Integer getLiftTime() {
        return liftTime;
    }

    public String getSeasonID() {
        return seasonID;
    }

    public String getDayID() {
        return dayID;
    }

    public String getSkierID() {
        return skierID;
    }

    public String getResortID() { return resortID; }

    public Integer getVertical() {
        return vertical;
    }
}
//import com.google.gson.annotations.SerializedName;
//
//public class LiftRide {
//
//    private int time;
//    @SerializedName("liftID")
//    private int liftId;
//    private int waitTime;
//
//    public LiftRide(int time, int liftId, int waitTime) {
//        this.time = time;
//        this.liftId = liftId;
//        this.waitTime = waitTime;
//    }
//
//    public int getTime() {
//        return time;
//    }
//
//    public void setTime(int time) {
//        this.time = time;
//    }
//
//    public int getLiftId() {
//        return liftId;
//    }
//
//    public void setLiftId(int liftId) {
//        this.liftId = liftId;
//    }
//
//    public int getWaitTime() {
//        return waitTime;
//    }
//
//    public void setWaitTime(int waitTime) {
//        this.waitTime = waitTime;
//    }
//}