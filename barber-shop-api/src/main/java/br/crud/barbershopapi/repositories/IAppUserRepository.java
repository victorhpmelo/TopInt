package br.crud.barbershopapi.repositories;

import br.crud.barbershopapi.models.AppUserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IAppUserRepository extends JpaRepository<AppUserModel, Long> {

    Optional<AppUserModel> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
}
