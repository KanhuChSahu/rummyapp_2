package com.rummy.repository;

import com.rummy.model.KycDocument;
import com.rummy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
    List<KycDocument> findByUser(User user);
    List<KycDocument> findByUserAndDocumentType(User user, String documentType);
}