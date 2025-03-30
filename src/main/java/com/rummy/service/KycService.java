package com.rummy.service;

import com.rummy.dto.KycDocumentDto;
import com.rummy.model.KycDocument;
import com.rummy.model.KycStatus;
import com.rummy.model.User;
import com.rummy.repository.KycDocumentRepository;
import com.rummy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class KycService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KycDocumentRepository kycDocumentRepository;

    private final Path documentStoragePath = Paths.get("uploads/kyc");

    public KycService() {
        try {
            Files.createDirectories(documentStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create document storage directory", e);
        }
    }

    public KycStatus getUserKycStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getKycStatus();
    }

    public KycDocument uploadDocument(KycDocumentDto documentDto, String documentType) {
        User user = userRepository.findById(documentDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if document already exists
        List<KycDocument> existingDocs = kycDocumentRepository.findByUserAndDocumentType(user, documentType);
        if (!existingDocs.isEmpty()) {
            throw new IllegalArgumentException("Document type already uploaded");
        }

        // Validate document type and number
        if (!isValidDocumentType(documentType)) {
            throw new IllegalArgumentException("Invalid document type");
        }
        if (!isValidDocumentNumber(documentType, documentDto.getDocumentNumber())) {
            throw new IllegalArgumentException("Invalid document number format");
        }

        String fileName = UUID.randomUUID().toString() + "-" + documentDto.getDocumentFile().getOriginalFilename();
        Path filePath = documentStoragePath.resolve(fileName);

        try {
            Files.copy(documentDto.getDocumentFile().getInputStream(), filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not store the file", e);
        }

        KycDocument document = new KycDocument();
        document.setUser(user);
        document.setDocumentType(documentType);
        document.setDocumentNumber(documentDto.getDocumentNumber());
        document.setDocumentPath(fileName);
        document.setVerificationStatus(KycStatus.PENDING);

        user.setKycStatus(KycStatus.IN_PROGRESS);
        userRepository.save(user);

        return kycDocumentRepository.save(document);
    }

    public KycStatus verifyUserDocuments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<KycDocument> documents = kycDocumentRepository.findByUser(user);
        boolean hasAadhaar = documents.stream().anyMatch(doc -> doc.getDocumentType().equals("AADHAAR"));
        boolean hasPan = documents.stream().anyMatch(doc -> doc.getDocumentType().equals("PAN"));

        if (hasAadhaar && hasPan) {
            user.setKycStatus(KycStatus.IN_PROGRESS);
            userRepository.save(user);
            return KycStatus.IN_PROGRESS;
        }

        return user.getKycStatus();
    }

    public KycStatus updateUserKycStatus(long userId, KycStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setKycStatus(status);
        userRepository.save(user);
        return status;
    }

    public KycDocument verifyDocument(Long documentId, KycStatus status, String remarks) {
        KycDocument document = kycDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        document.setVerificationStatus(status);
        document.setVerificationRemarks(remarks);

        // Update user's KYC status based on all documents
        User user = document.getUser();
        List<KycDocument> allDocuments = kycDocumentRepository.findByUser(user);
        updateUserKycStatus(user, allDocuments);

        return kycDocumentRepository.save(document);
    }

    public List<KycDocument> getUserDocuments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return kycDocumentRepository.findByUser(user);
    }

    private boolean isValidDocumentType(String documentType) {
        return documentType != null && (
            documentType.equals("AADHAR") ||
            documentType.equals("PAN") ||
            documentType.equals("PASSPORT")
        );
    }

    private boolean isValidDocumentNumber(String documentType, String number) {
        if (documentType.equals("AADHAR")) {
            return number.matches("^[0-9]{12}$");
        } else if (documentType.equals("PAN")) {
            return number.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
        }
        return false;
    }

    private void updateUserKycStatus(User user, List<KycDocument> allDocuments) {
        boolean allVerified = true;
        boolean anyRejected = false;

        for (KycDocument doc : allDocuments) {
            if (doc.getVerificationStatus() == KycStatus.REJECTED) {
                anyRejected = true;
                break;
            } else if (doc.getVerificationStatus() != KycStatus.APPROVED) {
                allVerified = false;
            }
        }

        if (anyRejected) {
            user.setKycStatus(KycStatus.REJECTED);
        } else if (allVerified && !allDocuments.isEmpty()) {
            user.setKycStatus(KycStatus.APPROVED);
        } else {
            user.setKycStatus(KycStatus.IN_PROGRESS);
        }

        userRepository.save(user);
    }
}
