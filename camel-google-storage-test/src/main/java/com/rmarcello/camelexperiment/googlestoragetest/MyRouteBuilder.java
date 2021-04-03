package com.rmarcello.camelexperiment.googlestoragetest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.google.storage.GoogleCloudStorageConstants;
import org.apache.camel.component.google.storage.GoogleCloudStorageOperations;

import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.google.cloud.storage.Blob;

public class MyRouteBuilder extends RouteBuilder {

    final String sourceBucket = "sourcebucket576832";
    final String consumedBucket = "consumedbucket576832";
    final String resultBucket = "resultbucket576832";

    @Override
    public void configure() throws Exception {
        
        from("google-storage://" + sourceBucket
        + "?moveAfterRead=true"
        + "&destinationBucket=" + consumedBucket
        + "&autoCreateBucket=true"
        + "&deleteAfterRead=true"
        + "&includeBody=true"
        )
        .log("consumed file: ${header.CamelGoogleCloudStorageObjectName}")
        .to("direct:consumed");
        
        from("direct:consumed")
        .process( exchange -> {
            byte[] body = exchange.getIn().getBody(byte[].class);
            ByteArrayInputStream bias = new ByteArrayInputStream( body );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Thumbnails.of( bias )
                .size(50, 50)
                .toOutputStream(baos);
            exchange.getIn().setBody( baos.toByteArray() );
        })
        .to("direct:processed");

        from("direct:processed")
        .to("google-storage://" + resultBucket )
        .log("uploaded file object:${header.CamelGoogleCloudStorageObjectName} to ${header.CamelGoogleCloudStorageBucketName} bucket")
        .to("direct:result");
        

        from("direct:result")
        .process( exchange -> {
            Blob blob = exchange.getIn().getBody(Blob.class);
            exchange.getIn().setHeader(GoogleCloudStorageConstants.OBJECT_NAME, blob.getName() );
            exchange.getIn().setHeader(GoogleCloudStorageConstants.OPERATION, GoogleCloudStorageOperations.createDownloadLink);
            exchange.getIn().setHeader(GoogleCloudStorageConstants.DOWNLOAD_LINK_EXPIRATION_TIME, 600000L); //10 minutes
        })
        .to("google-storage://" + resultBucket )
        .log("URL for ${header.CamelGoogleCloudStorageBucketName}/${header.CamelGoogleCloudStorageObjectName}=${body}");
    }
}
