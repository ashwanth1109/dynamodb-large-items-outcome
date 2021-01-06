package com.hackathon.shared;

public class Constants {
    // force non-instantiability through the `private` constructor
    private Constants() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static final int DDB_ITEM_SIZE_LIMIT = 400;
    public static final int KB = 1024;
    public static final String PK = "pk";
    public static final String SK = "sk";
    public static final String TEXT = "text";
}
