package com.schoolSys.schooolSys.common.mapper;

import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Generic base mapper that all feature mappers extend.
 * <p>
 * Provides the standard conversion contract between an entity {@code E},
 * a request DTO {@code REQ}, and a response DTO {@code RES}.
 * Feature mappers inherit these methods and only need to declare
 * additional {@code @Mapping} annotations for non-trivial fields.
 * </p>
 *
 * @param <E>   the JPA entity type
 * @param <REQ> the request/input DTO type
 * @param <RES> the response/output DTO type
 */
public interface GenericMapper<E, REQ, RES> {

    /**
     * Converts an entity to a response DTO.
     *
     * @param entity the source entity
     * @return the response DTO
     */
    RES toResponseDTO(E entity);

    /**
     * Converts a request DTO to a new entity.
     *
     * @param dto the source request DTO
     * @return the entity
     */
    E toEntity(REQ dto);

    /**
     * Updates an existing entity in-place from a request DTO.
     *
     * @param dto    the source request DTO
     * @param entity the target entity to update
     */
    void updateEntity(REQ dto, @MappingTarget E entity);

    /**
     * Converts a list of entities to a list of response DTOs.
     *
     * @param entities the source list
     * @return list of response DTOs
     */
    List<RES> toResponseDTOList(List<E> entities);
}
