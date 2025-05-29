package team.budderz.buddyspace.infra.database.neighborhood.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;

public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Long> {
}
