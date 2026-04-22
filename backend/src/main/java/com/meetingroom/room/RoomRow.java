package com.meetingroom.room;

public class RoomRow {

    private Long id;
    private String name;
    private String location;
    private int capacity;
    /** MySQL JSON 列原始字符串 */
    private String amenitiesJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getAmenitiesJson() {
        return amenitiesJson;
    }

    public void setAmenitiesJson(String amenitiesJson) {
        this.amenitiesJson = amenitiesJson;
    }
}
