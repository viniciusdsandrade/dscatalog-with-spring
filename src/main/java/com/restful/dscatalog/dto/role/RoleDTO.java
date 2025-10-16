package com.restful.dscatalog.dto.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.restful.dscatalog.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    @JsonProperty(access = WRITE_ONLY)
    private Long id;
    private String authority;

    public RoleDTO(Role role) {
        this.id = role.getId();
        this.authority = role.getAuthority();
    }
}
