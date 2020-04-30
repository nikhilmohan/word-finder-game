package com.learningenglish.wordfinder.domain;

public enum Category {

    FURNITURE("furniture"),
    HOSPITAL("hospital"),
    SCHOOL("school"),
    VEHICLE("vehicle"),
    ANIMAL("animal"),
    HOUSE("house"),
    FOOD("food"),
    COUNTRY("country"),
    SPORTS("sports"),
    DRESS("dress"),
    MOVIES("movies"),
    FARMING("farming"),
    STATIONERY("stationery"),
    FRUIT("fruit"),
    VEGETABLE("vegetable");

    private String topic;

    Category(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

}
