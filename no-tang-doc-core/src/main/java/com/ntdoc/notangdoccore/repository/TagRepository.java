package com.ntdoc.notangdoccore.repository;

import com.ntdoc.notangdoccore.entity.Document;
import com.ntdoc.notangdoccore.entity.Tag;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag,Long> {
   Optional<Tag> findByTag(String tag);

   @Query("SELECT d FROM Tag t JOIN t.documents d WHERE t.tag = :tagName AND d.uploadedBy.kcUserId = :userId")
   List<Document> findDocumentsByTagName(@Param("tagName") String tagName, @Param("userId") String kcUserId);
}
