package com.qassistant.context.db.repositories;

import com.qassistant.context.db.dbEntity.Project;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository
extends CrudRepository<Project, String> {
    public Optional<Project> findByName(String var1);
}
