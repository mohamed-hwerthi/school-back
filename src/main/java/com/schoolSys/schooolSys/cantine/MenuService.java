package com.schoolSys.schooolSys.cantine;

import java.util.UUID;

import com.schoolSys.schooolSys.cantine.dto.CreateMenuRequest;
import com.schoolSys.schooolSys.cantine.dto.MenuDTO;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    public List<MenuDTO> getAll() {
        return menuRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public MenuDTO getById(UUID id) {
        return toDto(menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", id)));
    }

    public List<MenuDTO> getMenuSemaine(Integer semaine) {
        return menuRepository.findBySemaineOrderByDateMenuAsc(semaine).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<MenuDTO> getMenuByDateRange(LocalDate start, LocalDate end) {
        return menuRepository.findByDateMenuBetweenOrderByDateMenuAsc(start, end).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuDTO create(CreateMenuRequest request) {
        Menu menu = Menu.builder()
                .dateMenu(request.getDateMenu())
                .jourSemaine(request.getJourSemaine())
                .entree(request.getEntree())
                .platPrincipal(request.getPlatPrincipal())
                .accompagnement(request.getAccompagnement())
                .dessert(request.getDessert())
                .allergenes(request.getAllergenes() != null ? request.getAllergenes().toArray(new String[0]) : null)
                .typeRegime(request.getTypeRegime() != null ? request.getTypeRegime() : "STANDARD")
                .semaine(request.getSemaine())
                .imageUrl(request.getImageUrl())
                .build();
        return toDto(menuRepository.save(menu));
    }

    @Transactional
    public MenuDTO update(UUID id, CreateMenuRequest request) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", id));
        menu.setDateMenu(request.getDateMenu());
        menu.setJourSemaine(request.getJourSemaine());
        menu.setEntree(request.getEntree());
        menu.setPlatPrincipal(request.getPlatPrincipal());
        menu.setAccompagnement(request.getAccompagnement());
        menu.setDessert(request.getDessert());
        menu.setAllergenes(request.getAllergenes() != null ? request.getAllergenes().toArray(new String[0]) : null);
        if (request.getTypeRegime() != null) menu.setTypeRegime(request.getTypeRegime());
        if (request.getSemaine() != null) menu.setSemaine(request.getSemaine());
        if (request.getImageUrl() != null) menu.setImageUrl(request.getImageUrl());
        return toDto(menuRepository.save(menu));
    }

    @Transactional
    public MenuDTO setImage(UUID id, String imageUrl) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", id));
        menu.setImageUrl(imageUrl);
        return toDto(menuRepository.save(menu));
    }

    @Transactional
    public void delete(UUID id) {
        if (!menuRepository.existsById(id)) {
            throw new ResourceNotFoundException("Menu", id);
        }
        menuRepository.deleteById(id);
    }

    private MenuDTO toDto(Menu m) {
        return MenuDTO.builder()
                .id(m.getId())
                .dateMenu(m.getDateMenu())
                .jourSemaine(m.getJourSemaine())
                .entree(m.getEntree())
                .platPrincipal(m.getPlatPrincipal())
                .accompagnement(m.getAccompagnement())
                .dessert(m.getDessert())
                .allergenes(m.getAllergenes() != null ? Arrays.asList(m.getAllergenes()) : List.of())
                .typeRegime(m.getTypeRegime())
                .semaine(m.getSemaine())
                .imageUrl(m.getImageUrl())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
