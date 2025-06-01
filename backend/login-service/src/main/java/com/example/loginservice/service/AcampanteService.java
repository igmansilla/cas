package com.example.loginservice.service;

import com.example.loginservice.model.Acampante;
import com.example.loginservice.repository.AcampanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AcampanteService {

    private final AcampanteRepository acampanteRepository;

    @Autowired
    public AcampanteService(AcampanteRepository acampanteRepository) {
        this.acampanteRepository = acampanteRepository;
    }

    @Transactional
    public Acampante createAcampante(Acampante acampante) {
        // Add any validation if needed, e.g., check for null or empty fields
        if (acampante.getNombreCompleto() == null || acampante.getNombreCompleto().trim().isEmpty()) {
            throw new IllegalArgumentException("Acampante name cannot be empty.");
        }
        // Further validation can be added here
        return acampanteRepository.save(acampante);
    }

    public List<Acampante> getAllAcampantes() {
        return acampanteRepository.findAll();
    }

    public Optional<Acampante> getAcampanteById(Long id) {
        return acampanteRepository.findById(id);
    }

    @Transactional
    public Acampante updateAcampante(Long id, Acampante acampanteDetails) {
        Acampante existingAcampante = acampanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Acampante not found with id: " + id));

        existingAcampante.setNombreCompleto(acampanteDetails.getNombreCompleto());
        existingAcampante.setEdad(acampanteDetails.getEdad());
        existingAcampante.setContactoEmergenciaNombre(acampanteDetails.getContactoEmergenciaNombre());
        existingAcampante.setContactoEmergenciaTelefono(acampanteDetails.getContactoEmergenciaTelefono());
        // Update other fields as needed, e.g.
        // existingAcampante.setDirigenteResponsable(acampanteDetails.getDirigenteResponsable());
        return acampanteRepository.save(existingAcampante);
    }

    @Transactional
    public void deleteAcampante(Long id) {
         if (!acampanteRepository.existsById(id)) {
            throw new RuntimeException("Acampante not found with id: " + id);
        }
        acampanteRepository.deleteById(id);
    }
}
