package com.schoolSys.schooolSys.tenant;

import com.schoolSys.schooolSys.common.mapper.GenericMapper;
import com.schoolSys.schooolSys.tenant.dto.TenantRequestDTO;
import com.schoolSys.schooolSys.tenant.dto.TenantResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for converting between {@link Tenant} entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface TenantMapper extends GenericMapper<Tenant, TenantRequestDTO, TenantResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Tenant toEntity(TenantRequestDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(TenantRequestDTO dto, @MappingTarget Tenant entity);
}
