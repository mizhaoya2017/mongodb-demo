package com.example.mongodbdemo.utils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.yuancore.radosgw.sdk.RGWPassport;

public class S3Client {
    public String serverAddress;
    public String accessKey;
    public String secretKey;

    public S3Client(String serverAddress, String accessKey, String secretKey) {
        this.serverAddress = serverAddress;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public AmazonS3 buildClient() {
        RGWPassport passport = new RGWPassport(serverAddress,accessKey,secretKey);
        AWSCredentials credentials = new BasicAWSCredentials(passport.getAccessKey(), passport.getSecretKey());
        ClientConfiguration conf = new ClientConfiguration();
        conf.setProtocol(Protocol.HTTP);
        conf.setSignerOverride("S3SignerType");
        try {
            AmazonS3 s3Client = new AmazonS3Client(credentials, conf);
            s3Client.setEndpoint(passport.getEndPoint());
            return s3Client;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
