package privateApp.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import privateApp.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUserId(Long userId); // Changé de User à Optional<User>
	}