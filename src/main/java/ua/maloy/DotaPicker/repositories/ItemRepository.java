package ua.maloy.DotaPicker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.maloy.DotaPicker.entity.Hero;
import ua.maloy.DotaPicker.entity.Item;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item,Long> {
    Optional<Item> findByDotaItemId(Long  dotaItemId);
}
