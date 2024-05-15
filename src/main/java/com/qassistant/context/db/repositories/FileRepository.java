package com.qassistant.context.db.repositories;

import com.qassistant.context.db.dbEntity.File;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository
extends CrudRepository<File, String> {
    public Optional<File> findByProjectIdAndChecksum(String var1, String var2);

    public int countFilesByProjectId(String var1);

    public Optional<File> findByProjectIdAndPath(String var1, String var2);

    public Optional<File> findByProjectIdAndName(String var1, String var2);

    public Set<File> findFilesByProjectId(String var1);

    public int deleteFilesByProjectId(String var1);

    public int deleteAllByProjectIdAndName(String var1, String var2);
}
