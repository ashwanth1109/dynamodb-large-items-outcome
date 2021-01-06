package com.hackathon.shared;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Random;

/**
 * Utility class to help with working with DynamoDB
 */
public class DynamoUtils {
    // force non-instantiability through the `private` constructor
    private DynamoUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    public static DynamoDB dynamoDB = new DynamoDB(client);

    public static int incrementAndGet(String tableName, ItemTypeInitial itemTypeInitial) {
        Table table = getTable(tableName);
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey(
                        "pk", "COUNTER",
                        "sk", "COUNTER#" + itemTypeInitial.getInitial()
                )
                .withUpdateExpression("SET #value = if_not_exists(#value, :start) + :inc")
                .withValueMap(new ValueMap().withInt(":inc", 1).withInt(":start", 0))
                .withNameMap(new NameMap().with("#value", "value"))
                .withReturnValues(ReturnValue.UPDATED_NEW);

        UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
        System.out.println("UpdateItem success: " + outcome.getItem().toJSONPretty());

        Gson gson = new GsonBuilder().create();
        OutcomeJson outcomeJson = gson.fromJson(outcome.getItem().toJSON(), OutcomeJson.class);

        // TODO: Check outcome for the value
        return outcomeJson.value;
    }

    public static Table getTable(String tableName) {
        return dynamoDB.getTable(tableName);
    }

    public static String getGeneratedLargeItem(int sizeInKb) {
        return getGeneratedLargeItem(sizeInKb, 0);
    }
    public static String getGeneratedLargeItem(int sizeInKb, int subtract) {
        var itemSize = sizeInKb * Constants.KB - subtract;
        Random rd = new Random();
        String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ";
        StringBuilder outputBuffer = new StringBuilder(itemSize);

        for (int i = 0; i < itemSize; i++){
            char letter = abc.charAt(rd.nextInt(abc.length()));
            outputBuffer.append(letter);
        }
        return outputBuffer.toString();
    }
}
