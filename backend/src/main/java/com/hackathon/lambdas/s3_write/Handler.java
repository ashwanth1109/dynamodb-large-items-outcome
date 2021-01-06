package com.hackathon.lambdas.s3_write;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.hackathon.shared.DynamoUtils;
import com.hackathon.shared.ItemTypeInitial;
import com.hackathon.shared.PerformanceService;
import com.hackathon.shared.ResponseFactory;
import com.hackathon.shared.S3Response;
import com.hackathon.shared.S3Utils;

import static com.hackathon.shared.Constants.PK;
import static com.hackathon.shared.Constants.SK;
import static com.hackathon.shared.Constants.TEXT;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        int itemSize = Integer.parseInt(input.getBody());
        System.out.println("ITEM SIZE: " + itemSize);

        final String tableName = System.getenv("TABLE_NAME");
        Table table = DynamoUtils.getTable(tableName);

        final int counter = DynamoUtils.incrementAndGet(
                tableName, ItemTypeInitial.Bucket);

        S3Response response = new S3Response();

        String pkValue = String.format("%s#%s",
                ItemTypeInitial.Bucket.getInitial(), counter);
        String skValue = "1";

        int sumOfItemPropAndValueLength = PK.length() + pkValue.length()
                + SK.length() + skValue.length() + TEXT.length();
        final String textVal = DynamoUtils
                .getGeneratedLargeItem(itemSize, sumOfItemPropAndValueLength);

        try {
            // [START] performance measurement
            PerformanceService performance = new PerformanceService("Bucket");
            String textUrl = S3Utils.putObjectToS3(pkValue, textVal);

            Item item = new Item()
                    .withPrimaryKey(PK, pkValue, SK, skValue)
                    .withString(TEXT, textUrl);

            table.putItem(item);

            // [END] performance measurement
            performance.end();
            response.setDuration(performance.measure());
        } catch (Exception e) {
            System.out.println("Error was thrown during compression");
            e.printStackTrace();

            response.setError(e.getMessage());
        }

        return ResponseFactory.getResponse(response);
    }
}
