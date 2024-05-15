package com.qassistant.context.db.repositories;

import com.qassistant.context.db.dbEntity.FileSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FileSectionRepository
extends CrudRepository<FileSection, String> {
    @Query(value="SELECT fs FROM FileSection fs WHERE fs.file.project.id = :projectId or fs.file.project.name = :projectId")
    public Page<FileSection> findAllByProjectId(@Param(value="projectId") String var1, Pageable var2);
}
