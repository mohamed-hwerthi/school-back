package com.schoolSys.schooolSys.notification;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.notification.dto.AnnonceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnonceService {

    private final AnnonceRepository annonceRepository;

    public List<AnnonceDTO> getActive() {
        return annonceRepository.findByActifTrueOrderByDatePublicationDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AnnonceDTO> getAll() {
        return annonceRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public AnnonceDTO getById(UUID id) {
        return toDto(annonceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce", id)));
    }

    public List<AnnonceDTO> getByType(String type) {
        return annonceRepository.findByTypeAndActifTrueOrderByDatePublicationDesc(type)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AnnonceDTO> getByDestinataires(List<String> destinataires) {
        return annonceRepository.findByDestinatairesInAndActifTrueOrderByDatePublicationDesc(destinataires)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnnonceDTO create(AnnonceDTO dto) {
        Annonce annonce = Annonce.builder()
                .titre(dto.getTitre())
                .contenu(dto.getContenu())
                .type(dto.getType() != null ? dto.getType() : "GENERAL")
                .destinataires(dto.getDestinataires() != null ? dto.getDestinataires() : "TOUS")
                .classeId(dto.getClasseId())
                .auteurId(dto.getAuteurId())
                .auteurName(dto.getAuteurName())
                .datePublication(dto.getDatePublication() != null ? dto.getDatePublication() : LocalDateTime.now())
                .dateExpiration(dto.getDateExpiration())
                .actif(true)
                .createdAt(LocalDateTime.now())
                .build();
        return toDto(annonceRepository.save(annonce));
    }

    @Transactional
    public AnnonceDTO update(UUID id, AnnonceDTO dto) {
        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce", id));

        if (dto.getTitre() != null) annonce.setTitre(dto.getTitre());
        if (dto.getContenu() != null) annonce.setContenu(dto.getContenu());
        if (dto.getType() != null) annonce.setType(dto.getType());
        if (dto.getDestinataires() != null) annonce.setDestinataires(dto.getDestinataires());
        if (dto.getClasseId() != null) annonce.setClasseId(dto.getClasseId());
        if (dto.getDateExpiration() != null) annonce.setDateExpiration(dto.getDateExpiration());

        return toDto(annonceRepository.save(annonce));
    }

    @Transactional
    public void softDelete(UUID id) {
        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce", id));
        annonce.setActif(false);
        annonceRepository.save(annonce);
    }

    private AnnonceDTO toDto(Annonce a) {
        return AnnonceDTO.builder()
                .id(a.getId())
                .titre(a.getTitre())
                .contenu(a.getContenu())
                .type(a.getType())
                .destinataires(a.getDestinataires())
                .classeId(a.getClasseId())
                .auteurId(a.getAuteurId())
                .auteurName(a.getAuteurName())
                .datePublication(a.getDatePublication())
                .dateExpiration(a.getDateExpiration())
                .actif(a.getActif())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
