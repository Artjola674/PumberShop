package com.ikubinfo.plumbershop.auth.repo;

import com.ikubinfo.plumbershop.auth.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken,String> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(String userId);
}
