package com.ikubinfo.plumbershop.user.repo;

import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<UserDocument, String>, QuerydslPredicateExecutor<UserDocument> {

    Optional<UserDocument> findUserByEmail(String email);

    boolean existsByEmail(String email);

    List<UserDocument> findAllByRole(Role role);
}
