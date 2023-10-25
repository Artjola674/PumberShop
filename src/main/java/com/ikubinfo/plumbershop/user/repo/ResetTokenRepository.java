package com.ikubinfo.plumbershop.user.repo;

import com.ikubinfo.plumbershop.user.model.ResetTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ResetTokenRepository extends MongoRepository<ResetTokenDocument,String> {
    Optional<ResetTokenDocument> findByToken(String token);
}
