package com.example.login.repository;

import com.example.login.model.Dirigente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DirigenteRepository extends JpaRepository<Dirigente, Long> {
    Optional<Dirigente> findByUserAccount_Username(String username);
}
