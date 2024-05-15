package com.qassistant.context.configs;

import org.neo4j.driver.Driver;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.Neo4jVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="spring.neo4j")
public class VectorStoreConfig {
    private String databaseName;
    private int embeddingDimension = 1536;
    private Neo4jVectorStore.Neo4jDistanceType distanceType;
    private String label;
    private String embeddingProperty;
    private String indexName;

    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public int getEmbeddingDimension() {
        return this.embeddingDimension;
    }

    public void setEmbeddingDimension(int embeddingDimension) {
        this.embeddingDimension = embeddingDimension;
    }

    public Neo4jVectorStore.Neo4jDistanceType getDistanceType() {
        return this.distanceType;
    }

    public void setDistanceType(Neo4jVectorStore.Neo4jDistanceType distanceType) {
        this.distanceType = distanceType;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getEmbeddingProperty() {
        return this.embeddingProperty;
    }

    public void setEmbeddingProperty(String embeddingProperty) {
        this.embeddingProperty = embeddingProperty;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @Bean
    @ConditionalOnProperty(prefix="spring.neo4j", name={"uri"})
    public Neo4jVectorStore vectorStore(Driver driver, EmbeddingClient embeddingClient) {
        Neo4jVectorStore.Neo4jVectorStoreConfig neo4jVectorStoreConfig = Neo4jVectorStore.Neo4jVectorStoreConfig.builder().withEmbeddingDimension(this.embeddingDimension).withDistanceType(this.distanceType).withLabel(this.label).withEmbeddingProperty(this.embeddingProperty).withIndexName(this.indexName).build();
        return new Neo4jVectorStore(driver, embeddingClient, neo4jVectorStoreConfig);
    }
}
