package privateApp.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import privateApp.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUserId(Long userId); // Changé de User à Optional<User>

	Optional<User> findTopByOrderByUserIdDesc();

	List<User> findByProfils_LibelleProfil(String libelleProfil);
	
	/**
     * Trouve les utilisateurs non archivés ayant un profil spécifique.
     * @param libelleProfil Le libellé du profil (ex. PERSONNEL_MEDICAL).
     * @return Liste des utilisateurs correspondants.
     */
    @Query("SELECT u FROM User u JOIN u.profils p WHERE p.libelleProfil = :libelleProfil AND u.archived = 0")
    List<User> findNonArchivedByProfil(String libelleProfil);
	
	}