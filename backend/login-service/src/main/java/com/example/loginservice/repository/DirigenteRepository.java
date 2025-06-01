package com.example.loginservice.repository;

import com.example.loginservice.model.Dirigente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DirigenteRepository extends JpaRepository<Dirigente, Long> {
    Optional<Dirigente> findByUserAccount_Username(String username);
}
