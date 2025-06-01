package com.example.loginservice.repository;

import com.example.loginservice.model.Acampante;
import org.springframework.data.jpa.repository.JpaRepository;
// import java.util.List;

public interface AcampanteRepository extends JpaRepository<Acampante, Long> {
    // Example: List<Acampante> findByEdadGreaterThan(int edad);
}
