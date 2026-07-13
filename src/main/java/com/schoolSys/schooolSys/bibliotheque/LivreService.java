package com.schoolSys.schooolSys.bibliotheque;

import java.util.UUID;

import com.schoolSys.schooolSys.bibliotheque.dto.CreateLivreRequest;
import com.schoolSys.schooolSys.bibliotheque.dto.LivreDTO;
import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LivreService {

    private final LivreRepository livreRepository;

    public PagedResponse<LivreDTO> findAll(String search, String categorie, Pageable pageable) {
        Page<Livre> page;

        if (search != null && !search.isBlank() && categorie != null && !categorie.isBlank()) {
            page = livreRepository
                    .findByCategorieAndTitreContainingIgnoreCaseOrCategorieAndAuteurContainingIgnoreCase(
                            categorie, search, categorie, search, pageable);
        } else if (search != null && !search.isBlank()) {
            page = livreRepository
                    .findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCase(search, search, pageable);
        } else if (categorie != null && !categorie.isBlank()) {
            page = livreRepository.findByCategorie(categorie, pageable);
        } else {
            page = livreRepository.findAll(pageable);
        }

        List<LivreDTO> content = page.getContent().stream()
                .map(this::toDTO)
                .toList();

        return PagedResponse.from(page, content);
    }

    public LivreDTO findById(UUID id) {
        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre", id));
        return toDTO(livre);
    }

    public List<String> findAllCategories() {
        return livreRepository.findAll().stream()
                .map(Livre::getCategorie)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional
    public LivreDTO create(CreateLivreRequest request) {
        Livre livre = Livre.builder()
                .titre(request.getTitre())
                .auteur(request.getAuteur())
                .isbn(request.getIsbn())
                .categorie(request.getCategorie())
                .editeur(request.getEditeur())
                .anneePublication(request.getAnneePublication())
                .description(request.getDescription())
                .nombreExemplaires(request.getNombreExemplaires() != null ? request.getNombreExemplaires() : 1)
                .exemplairesDisponibles(request.getNombreExemplaires() != null ? request.getNombreExemplaires() : 1)
                .emplacement(request.getEmplacement())
                .imageUrl(request.getImageUrl())
                .build();

        return toDTO(livreRepository.save(livre));
    }

    @Transactional
    public LivreDTO update(UUID id, CreateLivreRequest request) {
        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre", id));

        // Calculate the difference in total copies to adjust available copies
        int oldTotal = livre.getNombreExemplaires() != null ? livre.getNombreExemplaires() : 1;
        int newTotal = request.getNombreExemplaires() != null ? request.getNombreExemplaires() : oldTotal;
        int diff = newTotal - oldTotal;
        int newDisponibles = (livre.getExemplairesDisponibles() != null ? livre.getExemplairesDisponibles() : 0) + diff;
        if (newDisponibles < 0) newDisponibles = 0;

        livre.setTitre(request.getTitre());
        livre.setAuteur(request.getAuteur());
        livre.setIsbn(request.getIsbn());
        livre.setCategorie(request.getCategorie());
        livre.setEditeur(request.getEditeur());
        livre.setAnneePublication(request.getAnneePublication());
        livre.setDescription(request.getDescription());
        livre.setNombreExemplaires(newTotal);
        livre.setExemplairesDisponibles(newDisponibles);
        livre.setEmplacement(request.getEmplacement());
        livre.setImageUrl(request.getImageUrl());

        return toDTO(livreRepository.save(livre));
    }

    @Transactional
    public void delete(UUID id) {
        if (!livreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Livre", id);
        }
        livreRepository.deleteById(id);
    }

    // ─── Mapping ────────────────────────────────────────────

    LivreDTO toDTO(Livre livre) {
        return LivreDTO.builder()
                .id(livre.getId())
                .titre(livre.getTitre())
                .auteur(livre.getAuteur())
                .isbn(livre.getIsbn())
                .categorie(livre.getCategorie())
                .editeur(livre.getEditeur())
                .anneePublication(livre.getAnneePublication())
                .description(livre.getDescription())
                .nombreExemplaires(livre.getNombreExemplaires())
                .exemplairesDisponibles(livre.getExemplairesDisponibles())
                .emplacement(livre.getEmplacement())
                .imageUrl(livre.getImageUrl())
                .createdAt(livre.getCreatedAt())
                .updatedAt(livre.getUpdatedAt())
                .build();
    }
}
