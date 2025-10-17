package com.restful.dscatalog.dto.user;

import com.restful.dscatalog.dto.role.RoleDTO;
import com.restful.dscatalog.entity.User;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    @Setter(AccessLevel.NONE)
    private Set<RoleDTO> roles = new HashSet<>();

    public UserDTO(User user) {
        super();
        this.id = user.getId();
        this.firstName = htmlEscape(user.getFirstName());
        this.lastName = htmlEscape(user.getLastName());
        this.email = htmlEscape(user.getEmail());
        user.getRoles().forEach(role -> this.roles.add(new RoleDTO(role)));
    }
}
