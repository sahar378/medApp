package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import privateApp.models.Profil;

public interface ProfilRepository extends JpaRepository<Profil, Long> {
}