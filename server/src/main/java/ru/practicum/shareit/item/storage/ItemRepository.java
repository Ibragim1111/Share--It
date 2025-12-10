package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerIdOrderById(Long ownerId);

    @Query("""
            SELECT i FROM Item i\s
            WHERE i.available = true\s
            AND (i.name ILIKE %:text%\s
            OR i.description ILIKE %:text%)
            \s""")
    List<Item> searchAvailableItems(String text);

    // В ItemRepository
    List<Item> findByRequestId(Long requestId);
}