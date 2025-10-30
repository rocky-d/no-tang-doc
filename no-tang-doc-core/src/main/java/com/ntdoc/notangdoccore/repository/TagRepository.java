package com.ntdoc.notangdoccore.repository;

import com.ntdoc.notangdoccore.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag,Long> {
   Optional<Tag> findByTag(String tag);
}
