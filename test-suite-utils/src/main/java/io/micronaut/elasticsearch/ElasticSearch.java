package io.micronaut.elasticsearch;


import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class ElasticSearch {

    private static final String IMAGE_NAME = "docker.elastic.co/elasticsearch/elasticsearch:9.2.3";
    private static ElasticsearchContainer container;

    public static Map<String, String> getProperties() {
        if (container == null) {
            container = new ElasticsearchContainer(DockerImageName.parse(IMAGE_NAME));
            container.addEnv("xpack.security.enabled", "false");
            container.start();
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while(!container.isRunning());
            return getProperties(container);
        } else {
            return getProperties(container);
        }
    }

    private static Map<String, String> getProperties(ElasticsearchContainer container) {
        return Map.of(
            "elasticsearch.http-hosts", "http://" + container.getHttpHostAddress(),
            "elasticsearch.default.http-hosts", "http://" + container.getHttpHostAddress()
        );
    }
}
