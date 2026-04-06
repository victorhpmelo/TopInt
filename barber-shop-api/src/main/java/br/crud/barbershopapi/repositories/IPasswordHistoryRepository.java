package br.crud.barbershopapi.repositories;

import br.crud.barbershopapi.models.PasswordHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPasswordHistoryRepository extends JpaRepository<PasswordHistoryModel, Long> {

    List<PasswordHistoryModel> findTop3ByUser_IdOrderByCreatedAtDesc(Long userId);

    List<PasswordHistoryModel> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
