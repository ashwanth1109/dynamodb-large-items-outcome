package com.hackathon.deploy;

public final class Constants {
    private Constants() {
        throw new UnsupportedOperationException("Cannot instantiate this statics only class");
    }

    public static final String BACKEND_STACK = "backend-stack";

    public static final String DDB_LARGE_BUCKET_SUFFIX = "large-items-bucket";
    public static final String DDB_LARGE_TABLE_SUFFIX = "large-items-table";
}
