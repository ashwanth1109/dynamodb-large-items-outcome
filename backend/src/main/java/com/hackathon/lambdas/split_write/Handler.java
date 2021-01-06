package com.hackathon.lambdas.split_write;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.hackathon.shared.DynamoUtils;
import com.hackathon.shared.ItemTypeInitial;
import com.hackathon.shared.PerformanceService;
import com.hackathon.shared.ResponseFactory;
import com.hackathon.shared.SplitResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.hackathon.shared.Constants.DDB_ITEM_SIZE_LIMIT;
import static com.hackathon.shared.Constants.PK;
import static com.hackathon.shared.Constants.SK;
import static com.hackathon.shared.Constants.TEXT;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        int itemSize = Integer.parseInt(input.getBody());
        System.out.println("ITEM SIZE: " + itemSize);

        final String tableName = System.getenv("TABLE_NAME");

        SplitResponse response = new SplitResponse();

        final int counter = DynamoUtils.incrementAndGet(tableName, ItemTypeInitial.Split);
        String pkValue = String.format("%s#%s", ItemTypeInitial.Split.getInitial(), counter);
        final int numberOfRecords = (int) Math.ceil((double) itemSize / DDB_ITEM_SIZE_LIMIT);
        System.out.println("numberOfRecords:::: " + numberOfRecords);

        Collection<Item> items = new ArrayList<>();
        List<String> skValues = new ArrayList<>();
        List<String> textValues = new ArrayList<>();

        for (int i = 0; i < numberOfRecords; i++) {
            String skValue = Integer.toString(i);
            int sumOfItemPropAndValueLength = PK.length() + pkValue.length()
                    + SK.length() + skValue.length() + TEXT.length();
            int iterationItemSize = i + 1 == numberOfRecords
                    ? DDB_ITEM_SIZE_LIMIT - (DDB_ITEM_SIZE_LIMIT * numberOfRecords - itemSize) // last item size
                    : DDB_ITEM_SIZE_LIMIT;
            final String textValue = DynamoUtils
                    .getGeneratedLargeItem(iterationItemSize, sumOfItemPropAndValueLength);
            textValues.add(textValue);
            skValues.add(skValue);
            System.out.println("ITERATION::: " + i);
            System.out.println("text LENGTH::: " + textValue.length());
        }

        try {
            // [START] performance measurement
            PerformanceService performance = new PerformanceService("Split");

            for (int i = 0; i < numberOfRecords; i++) {
                String textValue = textValues.get(i);
                String skValue = skValues.get(i);

                Item item = new Item()
                    .withPrimaryKey(PK, pkValue, SK, skValue)
                    .withString(TEXT, textValue);

                items.add(item);
            }

            var tableWriteItems = new TableWriteItems(tableName);
            tableWriteItems.withItemsToPut(items);
            var unprocessedItems = DynamoUtils.dynamoDB
                .batchWriteItem(tableWriteItems)
                .getUnprocessedItems();

            if (!unprocessedItems.isEmpty()) {
                batchWriteItemUnprocessedRecursive(unprocessedItems);
            }

            // [END] performance measurement
            performance.end();
            response.setDuration(performance.measure());
            response.setNumberOfRecords(numberOfRecords);
        } catch (Exception e) {
            System.out.println("Error was thrown during splitting data");
            e.printStackTrace();

            response.setError(e.getMessage());
        }

        return ResponseFactory.getResponse(response);
    }

    private static void batchWriteItemUnprocessedRecursive(Map<String, List<WriteRequest>> unprocessedItems) {
        var moreUnprocessedItems = DynamoUtils.dynamoDB
            .batchWriteItemUnprocessed(unprocessedItems)
            .getUnprocessedItems();

        if (!moreUnprocessedItems.isEmpty()) {
            batchWriteItemUnprocessedRecursive(moreUnprocessedItems);
        }
    }
}
