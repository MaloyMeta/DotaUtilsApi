package ua.maloy.DotaPicker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.maloy.DotaPicker.entity.Hero;

import java.util.Optional;

public interface HeroRepository extends JpaRepository<Hero, Long> {
    Optional<Hero> findByLocalizedName(String name);

    Optional<Hero> findByOpenDotaId(long enemyId);
}
