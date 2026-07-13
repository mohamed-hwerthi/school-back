package com.schoolSys.schooolSys.devoir;

import java.util.UUID;

import com.schoolSys.schooolSys.common.annee.AnneeScolaireProvider;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.devoir.dto.CreateRessourceRequest;
import com.schoolSys.schooolSys.devoir.dto.RessourceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RessourceService {

    private final RessourcePedagogiqueRepository ressourceRepository;
    private final AnneeScolaireProvider anneeScolaireProvider;

    public List<RessourceDTO> findAll(UUID moduleId, String anneeScolaire) {
        String year = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        List<RessourcePedagogique> ressources;
        if (moduleId != null) {
            ressources = ressourceRepository.findByModuleIdAndAnneeScolaireOrderByCreatedAtDesc(moduleId, year);
        } else {
            ressources = ressourceRepository.findByAnneeScolaireOrderByCreatedAtDesc(year);
        }
        return ressources.stream().map(this::toDTO).toList();
    }

    public RessourceDTO findById(UUID id) {
        RessourcePedagogique ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource", id));
        return toDTO(ressource);
    }

    public List<RessourceDTO> findByModule(UUID moduleId) {
        return ressourceRepository.findByModuleIdOrderByCreatedAtDesc(moduleId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public RessourceDTO create(CreateRessourceRequest request) {
        RessourcePedagogique ressource = RessourcePedagogique.builder()
                .titre(request.getTitre())
                .description(request.getDescription())
                .moduleId(request.getModuleId())
                .type(request.getType())
                .fichierUrl(request.getFichierUrl())
                .lienExterne(request.getLienExterne())
                .enseignantId(request.getEnseignantId())
                .tailleFichier(request.getTailleFichier())
                .anneeScolaire(anneeScolaireProvider.resolveAnneeScolaire(request.getAnneeScolaire()))
                .build();
        return toDTO(ressourceRepository.save(ressource));
    }

    @Transactional
    public RessourceDTO update(UUID id, CreateRessourceRequest request) {
        RessourcePedagogique ressource = ressourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource", id));

        ressource.setTitre(request.getTitre());
        ressource.setDescription(request.getDescription());
        ressource.setModuleId(request.getModuleId());
        ressource.setType(request.getType());
        ressource.setFichierUrl(request.getFichierUrl());
        ressource.setLienExterne(request.getLienExterne());
        ressource.setEnseignantId(request.getEnseignantId());
        ressource.setTailleFichier(request.getTailleFichier());
        if (request.getAnneeScolaire() != null) {
            ressource.setAnneeScolaire(request.getAnneeScolaire());
        }

        return toDTO(ressourceRepository.save(ressource));
    }

    @Transactional
    public void delete(UUID id) {
        if (!ressourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ressource", id);
        }
        ressourceRepository.deleteById(id);
    }

    private RessourceDTO toDTO(RessourcePedagogique ressource) {
        return RessourceDTO.builder()
                .id(ressource.getId())
                .titre(ressource.getTitre())
                .description(ressource.getDescription())
                .moduleId(ressource.getModuleId())
                .type(ressource.getType())
                .fichierUrl(ressource.getFichierUrl())
                .lienExterne(ressource.getLienExterne())
                .enseignantId(ressource.getEnseignantId())
                .tailleFichier(ressource.getTailleFichier())
                .anneeScolaire(ressource.getAnneeScolaire())
                .createdAt(ressource.getCreatedAt())
                .build();
    }
}
