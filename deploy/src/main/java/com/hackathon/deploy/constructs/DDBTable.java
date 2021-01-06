package com.hackathon.deploy.constructs;

import lombok.Getter;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;

import static com.hackathon.deploy.Constants.DDB_LARGE_TABLE_SUFFIX;
import static com.hackathon.deploy.DeploymentManager.withEnv;

public class DDBTable extends Construct {
    @Getter
    private final String tableName;

    public DDBTable(Construct scope, String id) {
        super(scope, id);

        tableName = withEnv(DDB_LARGE_TABLE_SUFFIX);

        Attribute partitionKey = Attribute.builder()
                .name("pk")
                .type(AttributeType.STRING)
                .build();

        Attribute sortKey = Attribute.builder()
                .name("sk")
                .type(AttributeType.STRING)
                .build();

        TableProps tableProps = TableProps.builder()
                .tableName(tableName)
                .partitionKey(partitionKey)
                .sortKey(sortKey)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        new Table(scope, tableName, tableProps);
    }
}
