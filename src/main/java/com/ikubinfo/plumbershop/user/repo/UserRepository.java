package com.ikubinfo.plumbershop.user.repo;

import com.ikubinfo.plumbershop.user.model.UserDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findUserByEmail(String email);

    boolean existsByEmail(String email);

    Page<UserDocument> findPageByEmailStartingWith( String email, Pageable pageable);
}
