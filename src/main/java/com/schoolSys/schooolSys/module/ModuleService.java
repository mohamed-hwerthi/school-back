package com.schoolSys.schooolSys.module;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.domaine.Domaine;
import com.schoolSys.schooolSys.domaine.DomaineRepository;
import com.schoolSys.schooolSys.domaine.SousDomaine;
import com.schoolSys.schooolSys.domaine.SousDomaineRepository;
import com.schoolSys.schooolSys.examen.ExamenService;
import com.schoolSys.schooolSys.module.dto.ModuleRequestDTO;
import com.schoolSys.schooolSys.module.dto.ModuleResponseDTO;
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
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final NiveauRepository niveauRepository;
    private final DomaineRepository domaineRepository;
    private final SousDomaineRepository sousDomaineRepository;
    private final ExamenService examenService;
    private final CurrentUserContext currentUser;

    public List<ModuleResponseDTO> findAll(UUID niveauId) {
        List<Module> modules;
        if (niveauId != null) {
            modules = moduleRepository.findByNiveauIdOrderByOrdreEtatiqueAsc(niveauId);
        } else {
            modules = moduleRepository.findAllByOrderByNiveauNameAscOrdreEtatiqueAsc();
        }
        // Row-level scoping: an ENSEIGNANT only sees the matières he is affected to teach.
        if (currentUser.hasRole(UserRole.ENSEIGNANT)) {
            Set<UUID> scoped = currentUser.getScopedModuleIdsForTeacher();
            modules = modules.stream().filter(m -> scoped.contains(m.getId())).toList();
        }
        return modules.stream().map(this::toResponse).toList();
    }

    public ModuleResponseDTO findById(UUID id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module", id));
        return toResponse(module);
    }

    @Transactional
    public ModuleResponseDTO create(ModuleRequestDTO dto) {
        Niveau niveau = niveauRepository.findById(dto.getNiveauId())
                .orElseThrow(() -> new ResourceNotFoundException("Niveau", dto.getNiveauId()));

        Module module = Module.builder()
                .name(dto.getName())
                .nameVp(dto.getNameVp())
                .nameAr(dto.getNameAr())
                .coeffEtatique(dto.getCoeffEtatique())
                .coeffPrive(dto.getCoeffPrive())
                .ordreEtatique(dto.getOrdreEtatique())
                .ordrePrive(dto.getOrdrePrive())
                .niveau(niveau)
                .domaine(resolveDomaine(dto.getDomaineId()))
                .sousDomaine(resolveSousDomaine(dto.getSousDomaineId()))
                .versionEtatique(dto.getVersionEtatique())
                .versionPrivee(dto.getVersionPrivee())
                .salleTypeRequise(dto.getSalleTypeRequise())
                .dureeMinSeance(dto.getDureeMinSeance())
                .dureeMaxSeance(dto.getDureeMaxSeance())
                .preferenceHoraire(dto.getPreferenceHoraire())
                .build();

        Module saved = moduleRepository.save(module);
        // Each new matière gets a default exam grid: one exam per (classe × trimestre).
        examenService.createDefaultsForModule(saved);
        return toResponse(saved);
    }

    @Transactional
    public ModuleResponseDTO update(UUID id, ModuleRequestDTO dto) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module", id));

        if (!module.getNiveau().getId().equals(dto.getNiveauId())) {
            Niveau niveau = niveauRepository.findById(dto.getNiveauId())
                    .orElseThrow(() -> new ResourceNotFoundException("Niveau", dto.getNiveauId()));
            module.setNiveau(niveau);
        }

        module.setName(dto.getName());
        module.setNameVp(dto.getNameVp());
        module.setNameAr(dto.getNameAr());
        module.setCoeffEtatique(dto.getCoeffEtatique());
        module.setCoeffPrive(dto.getCoeffPrive());
        module.setOrdreEtatique(dto.getOrdreEtatique());
        module.setOrdrePrive(dto.getOrdrePrive());
        module.setDomaine(resolveDomaine(dto.getDomaineId()));
        module.setSousDomaine(resolveSousDomaine(dto.getSousDomaineId()));
        module.setVersionEtatique(dto.getVersionEtatique());
        module.setVersionPrivee(dto.getVersionPrivee());
        module.setSalleTypeRequise(dto.getSalleTypeRequise());
        module.setDureeMinSeance(dto.getDureeMinSeance());
        module.setDureeMaxSeance(dto.getDureeMaxSeance());
        module.setPreferenceHoraire(dto.getPreferenceHoraire());

        return toResponse(moduleRepository.save(module));
    }

    @Transactional
    public void delete(UUID id) {
        if (!moduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Module", id);
        }
        moduleRepository.deleteById(id);
    }

    private Domaine resolveDomaine(UUID domaineId) {
        if (domaineId == null) return null;
        return domaineRepository.findById(domaineId)
                .orElseThrow(() -> new ResourceNotFoundException("Domaine", domaineId));
    }

    private SousDomaine resolveSousDomaine(UUID sousDomaineId) {
        if (sousDomaineId == null) return null;
        return sousDomaineRepository.findById(sousDomaineId)
                .orElseThrow(() -> new ResourceNotFoundException("SousDomaine", sousDomaineId));
    }

    private ModuleResponseDTO toResponse(Module module) {
        ModuleResponseDTO.ModuleResponseDTOBuilder builder = ModuleResponseDTO.builder()
                .id(module.getId())
                .name(module.getName())
                .nameVp(module.getNameVp())
                .nameAr(module.getNameAr())
                .coeffEtatique(module.getCoeffEtatique())
                .coeffPrive(module.getCoeffPrive())
                .ordreEtatique(module.getOrdreEtatique())
                .ordrePrive(module.getOrdrePrive())
                .niveauId(module.getNiveau().getId())
                .niveauName(module.getNiveau().getName())
                .versionEtatique(module.getVersionEtatique())
                .versionPrivee(module.getVersionPrivee())
                .salleTypeRequise(module.getSalleTypeRequise())
                .dureeMinSeance(module.getDureeMinSeance())
                .dureeMaxSeance(module.getDureeMaxSeance())
                .preferenceHoraire(module.getPreferenceHoraire());

        if (module.getDomaine() != null) {
            builder.domaineId(module.getDomaine().getId())
                    .domaineName(module.getDomaine().getName());
        }
        if (module.getSousDomaine() != null) {
            builder.sousDomaineId(module.getSousDomaine().getId())
                    .sousDomaineName(module.getSousDomaine().getName());
        }

        return builder.build();
    }
}
