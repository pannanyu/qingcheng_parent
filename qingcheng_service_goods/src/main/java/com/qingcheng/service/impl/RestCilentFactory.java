package com.qingcheng.service.impl;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

public class RestCilentFactory {

    public static RestHighLevelClient getRestCilentFactory(String hostname,int port){
        //连接rest接口
        HttpHost httpHost = new HttpHost(hostname, port, "http");
        RestClientBuilder builder = RestClient.builder(httpHost);
       return new RestHighLevelClient(builder);



    }
}
