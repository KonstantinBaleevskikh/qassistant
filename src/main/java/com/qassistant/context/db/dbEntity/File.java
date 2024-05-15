package com.qassistant.context.db.dbEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;

@Entity
@Table(name="file", indexes={@Index(name="idx_project", columnList="projectId")})
public class File {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private String id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="projectId", nullable=false)
    private Project project;
    private String path;
    private String name;
    private String checksum;
    @OneToMany(fetch=FetchType.LAZY, mappedBy="file", cascade={CascadeType.ALL}, orphanRemoval=true)
    private Set<FileSection> sections;

    public File() {
    }

    public File(Project project, String checksum, String path) {
        this.project = project;
        this.path = path;
        this.checksum = checksum;
        this.name = this.getName();
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        if (this.path != null && !this.path.isEmpty()) {
            return new java.io.File(this.path).getName();
        }
        return this.name;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getChecksum() {
        return this.checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Set<FileSection> getSections() {
        return this.sections;
    }

    public void setSections(Set<FileSection> sections) {
        this.sections = sections;
    }
}
