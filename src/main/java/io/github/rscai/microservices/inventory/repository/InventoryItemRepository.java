package io.github.rscai.microservices.inventory.repository;

import io.github.rscai.microservices.inventory.model.InventoryItem;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {

  Page<InventoryItem> findByProductIdIn(List<String> productIds, Pageable pageable);
}
