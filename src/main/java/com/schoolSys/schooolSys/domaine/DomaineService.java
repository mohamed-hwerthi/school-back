package com.schoolSys.schooolSys.domaine;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.domaine.dto.*;
import com.schoolSys.schooolSys.niveau.Niveau;
import com.schoolSys.schooolSys.niveau.NiveauRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DomaineService {

    private final DomaineRepository domaineRepository;
    private final SousDomaineRepository sousDomaineRepository;
    private final NiveauRepository niveauRepository;
    private final CurrentUserContext currentUser;

    // ── Domaines ────────────────────────────────────────────

    public List<DomaineResponseDTO> findAll(UUID niveauId) {
        List<Domaine> list = niveauId != null
                ? domaineRepository.findByNiveauIdOrderByOrdreAsc(niveauId)
                : domaineRepository.findAllByOrderByNiveauNameAscOrdreAsc();
        // Row-level scoping: an ENSEIGNANT only sees the domaines of his own subjects.
        if (currentUser.hasRole(UserRole.ENSEIGNANT)) {
            Set<UUID> scoped = currentUser.getScopedDomaineIdsForTeacher();
            list = list.stream().filter(d -> scoped.contains(d.getId())).toList();
        }
        return list.stream().map(this::toResponse).toList();
    }

    public DomaineResponseDTO findById(UUID id) {
        Domaine d = domaineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domaine", id));
        return toResponse(d);
    }

    @Transactional
    public DomaineResponseDTO create(DomaineRequestDTO dto) {
        Niveau niveau = niveauRepository.findById(dto.getNiveauId())
                .orElseThrow(() -> new ResourceNotFoundException("Niveau", dto.getNiveauId()));

        Domaine domaine = Domaine.builder()
                .name(dto.getName())
                .nameAr(dto.getNameAr())
                .ordre(dto.getOrdre())
                .coeffEtatique(dto.getCoeffEtatique())
                .coeffPrive(dto.getCoeffPrive())
                .versionEtatique(dto.getVersionEtatique())
                .versionPrivee(dto.getVersionPrivee())
                .niveau(niveau)
                .build();

        return toResponse(domaineRepository.save(domaine));
    }

    @Transactional
    public DomaineResponseDTO update(UUID id, DomaineRequestDTO dto) {
        Domaine domaine = domaineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domaine", id));

        if (!domaine.getNiveau().getId().equals(dto.getNiveauId())) {
            Niveau niveau = niveauRepository.findById(dto.getNiveauId())
                    .orElseThrow(() -> new ResourceNotFoundException("Niveau", dto.getNiveauId()));
            domaine.setNiveau(niveau);
        }

        domaine.setName(dto.getName());
        domaine.setNameAr(dto.getNameAr());
        domaine.setOrdre(dto.getOrdre());
        domaine.setCoeffEtatique(dto.getCoeffEtatique());
        domaine.setCoeffPrive(dto.getCoeffPrive());
        domaine.setVersionEtatique(dto.getVersionEtatique());
        domaine.setVersionPrivee(dto.getVersionPrivee());

        return toResponse(domaineRepository.save(domaine));
    }

    @Transactional
    public void delete(UUID id) {
        if (!domaineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Domaine", id);
        }
        domaineRepository.deleteById(id);
    }

    // ── Sous-domaines ───────────────────────────────────────

    public List<SousDomaineDTO> findSousDomaines(UUID domaineId) {
        return sousDomaineRepository.findByDomaineIdOrderByOrdreAsc(domaineId)
                .stream().map(this::toSousDomaineDTO).toList();
    }

    @Transactional
    public SousDomaineDTO createSousDomaine(SousDomaineRequestDTO dto) {
        Domaine domaine = domaineRepository.findById(dto.getDomaineId())
                .orElseThrow(() -> new ResourceNotFoundException("Domaine", dto.getDomaineId()));

        SousDomaine sd = SousDomaine.builder()
                .name(dto.getName())
                .nameAr(dto.getNameAr())
                .ordre(dto.getOrdre())
                .domaine(domaine)
                .build();

        return toSousDomaineDTO(sousDomaineRepository.save(sd));
    }

    @Transactional
    public SousDomaineDTO updateSousDomaine(UUID id, SousDomaineRequestDTO dto) {
        SousDomaine sd = sousDomaineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SousDomaine", id));

        if (!sd.getDomaine().getId().equals(dto.getDomaineId())) {
            Domaine domaine = domaineRepository.findById(dto.getDomaineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Domaine", dto.getDomaineId()));
            sd.setDomaine(domaine);
        }

        sd.setName(dto.getName());
        sd.setNameAr(dto.getNameAr());
        sd.setOrdre(dto.getOrdre());

        return toSousDomaineDTO(sousDomaineRepository.save(sd));
    }

    @Transactional
    public void deleteSousDomaine(UUID id) {
        if (!sousDomaineRepository.existsById(id)) {
            throw new ResourceNotFoundException("SousDomaine", id);
        }
        sousDomaineRepository.deleteById(id);
    }

    // ── Mapping ─────────────────────────────────────────────

    private DomaineResponseDTO toResponse(Domaine d) {
        return DomaineResponseDTO.builder()
                .id(d.getId())
                .name(d.getName())
                .nameAr(d.getNameAr())
                .ordre(d.getOrdre())
                .coeffEtatique(d.getCoeffEtatique())
                .coeffPrive(d.getCoeffPrive())
                .versionEtatique(d.getVersionEtatique())
                .versionPrivee(d.getVersionPrivee())
                .niveauId(d.getNiveau().getId())
                .niveauName(d.getNiveau().getName())
                .sousDomaines(d.getSousDomaines().stream().map(this::toSousDomaineDTO).toList())
                .build();
    }

    private SousDomaineDTO toSousDomaineDTO(SousDomaine sd) {
        return SousDomaineDTO.builder()
                .id(sd.getId())
                .name(sd.getName())
                .nameAr(sd.getNameAr())
                .ordre(sd.getOrdre())
                .domaineId(sd.getDomaine().getId())
                .build();
    }
}
