package com.hackathon.lambdas.compress_write;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.hackathon.shared.CompressResponse;
import com.hackathon.shared.DynamoUtils;
import com.hackathon.shared.ItemTypeInitial;
import com.hackathon.shared.PerformanceService;
import com.hackathon.shared.ResponseFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.hackathon.shared.Constants.KB;
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
                tableName, ItemTypeInitial.Compress);

        CompressResponse response = new CompressResponse();

        String pkValue = String.format("%s#%s",
                ItemTypeInitial.Compress.getInitial(), counter);
        String skValue = "1";
        int sumOfItemPropAndValueLength = PK.length() + pkValue.length()
                + SK.length() + skValue.length() + TEXT.length();

        final String textVal = DynamoUtils
                .getGeneratedLargeItem(itemSize, sumOfItemPropAndValueLength);

        try {
            // [START] performance measurement
            PerformanceService performance = new PerformanceService("Compress");
            ByteBuffer compressedMessage = compressString(textVal);
            response.setSize(String.format("%.2f", (double) compressedMessage.array().length / KB));

            Item item = new Item()
                    .withPrimaryKey(PK, pkValue, SK, skValue)
                    .withBinary(TEXT, compressedMessage);

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

    private static ByteBuffer compressString(String input) throws IOException {
        // Compress the UTF-8 encoded String into a byte[]
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPOutputStream os = new GZIPOutputStream(outputStream);
        os.write(input.getBytes(StandardCharsets.UTF_8));
        os.close();
        outputStream.close();
        byte[] compressedBytes = outputStream.toByteArray();

        // The following code writes the compressed bytes to a ByteBuffer.
        // A simpler way to do this is by simply calling
        // ByteBuffer.wrap(compressedBytes);
        // However, the longer form below shows the importance of resetting the
        // position of the buffer
        // back to the beginning of the buffer if you are writing bytes directly
        // to it, since the SDK
        // will consider only the bytes after the current position when sending
        // data to DynamoDB.
        // Using the "wrap" method automatically resets the position to zero.
        ByteBuffer buffer = ByteBuffer.allocate(compressedBytes.length);
        buffer.put(compressedBytes, 0, compressedBytes.length);
        buffer.position(0); // Important: reset the position of the ByteBuffer
        // to the beginning
        return buffer;
    }

    private static String uncompressString(ByteBuffer input) throws IOException {
        byte[] bytes = input.array();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPInputStream is = new GZIPInputStream(inputStream);

        int chunkSize = 1024;
        byte[] buffer = new byte[chunkSize];
        int length = 0;
        while ((length = is.read(buffer, 0, chunkSize)) != -1) {
            outputStream.write(buffer, 0, length);
        }

        String result = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

        is.close();
        outputStream.close();
        inputStream.close();

        return result;
    }
}
