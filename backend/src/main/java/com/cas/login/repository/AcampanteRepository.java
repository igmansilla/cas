package com.cas.login.repository;

import com.cas.login.model.Acampante;
import org.springframework.data.jpa.repository.JpaRepository;
// import java.util.List;

public interface AcampanteRepository extends JpaRepository<Acampante, Long> {
    // Example: List<Acampante> findByEdadGreaterThan(int edad);
}
