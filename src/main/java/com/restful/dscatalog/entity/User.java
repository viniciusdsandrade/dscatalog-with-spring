package com.restful.dscatalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.proxy.HibernateProxy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.NONE;


@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity
@Table(name = "tb_user",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_tb_user_email",
                columnNames = "email"
        )
)
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    @ToString.Exclude
    private String password;

    @BatchSize(size = 50)
    @ToString.Exclude
    @Setter(NONE)
    @JoinTable(
            name = "tb_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @ManyToMany(fetch = LAZY)
    private Set<Role> roles = new HashSet<>();

    public void initializeProfile(String firstName, String lastName, String normalizedEmail, String passwordHash) {
        this.firstName = requireNonBlank(firstName, "firstName");
        this.lastName = requireNonBlank(lastName, "lastName");
        this.email = requireNonBlank(normalizedEmail, "email");
        this.password = requireNonBlank(passwordHash, "passwordHash");
    }

    public void updateProfile(String firstName, String lastName, String normalizedEmail) {
        applyIfPresent(firstName, "firstName", v -> this.firstName = v);
        applyIfPresent(lastName, "lastName", v -> this.lastName = v);
        applyIfPresent(normalizedEmail, "email", v -> this.email = v);
    }

    private static String requireNonBlank(String v, String field) {
        if (v == null) throw new IllegalArgumentException(field + " required");
        v = v.strip();
        if (v.isEmpty()) throw new IllegalArgumentException(field + " blank");
        return v;
    }

    private static void applyIfPresent(String value, String field, java.util.function.Consumer<String> apply) {
        if (value == null) return;
        String v = value.strip();
        if (v.isEmpty()) throw new IllegalArgumentException(field + " blank");
        apply.accept(v);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
