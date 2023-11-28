package com.ikubinfo.plumbershop.user.repo;

import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void canFindUserByEmail() {

        UserDocument user = createUser("artjolakotorri@gmail.com", Role.SELLER);
        underTest.save(user);
        Optional<UserDocument> result = underTest.findUserByEmail(user.getEmail());
        assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void canNotFindUserByEmail() {
        String email = "artjolakotorri@gmail.com";
        UserDocument user = createUser("artjolakotorri1@gmail.com", Role.SELLER);
        underTest.save(user);
        Optional<UserDocument> result = underTest.findUserByEmail(email);
        assertThat(result).isEmpty();
    }

    @Test
    void checkIfEmailExists_returnTrue() {
        UserDocument user = createUser("artjolakotorri@gmail.com", Role.SELLER);
        underTest.save(user);
        boolean result = underTest.existsByEmail(user.getEmail());
        assertThat(result).isTrue();
    }

    @Test
    void checkIfEmailExists_returnFalse() {
        String email = "artjolakotorri@gmail.com";
        boolean result = underTest.existsByEmail(email);
        assertThat(result).isFalse();
    }

    @Test
    void canFindAllByRole() {
        UserDocument user1 = createUser("artjola1kotorri@gmail.com", Role.SELLER);
        UserDocument user2 = createUser("artjola2kotorri@gmail.com", Role.SELLER);
        UserDocument user3 = createUser("artjola3kotorri@gmail.com", Role.ADMIN);
        List<UserDocument> users = Arrays.asList(user1, user2, user3);

        underTest.saveAll(users);

        List<UserDocument> result = underTest.findAllByRole(Role.SELLER);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getRole()).isEqualTo(Role.SELLER);


    }

    @Test
    void canNotFindAllByRole() {
        UserDocument user1 = createUser("artjola1kotorri@gmail.com", Role.USER);
        UserDocument user2 = createUser("artjola2kotorri@gmail.com", Role.PLUMBER);
        UserDocument user3 = createUser("artjola3kotorri@gmail.com", Role.ADMIN);
        List<UserDocument> users = Arrays.asList(user1, user2, user3);

        underTest.saveAll(users);

        List<UserDocument> result = underTest.findAllByRole(Role.SELLER);

        assertThat(result.isEmpty()).isTrue();

    }

    private UserDocument createUser(String email, Role role) {
        UserDocument userDocument = new UserDocument();
        userDocument.setFirstName("Artjola");
        userDocument.setLastName("Kotorri");
        userDocument.setEmail(email);
        userDocument.setRole(role);
        return userDocument;
    }
}