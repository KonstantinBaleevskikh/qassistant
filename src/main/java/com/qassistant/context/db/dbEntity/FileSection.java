package com.qassistant.context.db.dbEntity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qassistant.context.utils.Mapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.util.List;

@Entity
public class FileSection {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private String id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="fileId", referencedColumnName="id", nullable=false)
    private File file;
    @Lob
    private String content;
    @Lob
    @Column(length=100000)
    private String embeddings;
    private double weight;

    public FileSection() {
    }

    public FileSection(File file, String content, List<Double> embeddings) {
        this.file = file;
        this.content = content;
        this.setEmbeddings(embeddings);
    }

    public FileSection(String content, List<Double> embeddings, int weight) {
        this.content = content;
        this.weight = weight;
        this.setEmbeddings(embeddings);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public File getFile() {
        return this.file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getWeight() {
        return this.weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setEmbeddings(List<Double> embeddings) {
        this.embeddings = Mapper.serialize(embeddings);
    }

    public List<Double> getEmbeddings() {
        return (List)Mapper.deserialize((String)this.embeddings, (TypeReference)new TypeReference<List<Double>>(){});
    }
}
