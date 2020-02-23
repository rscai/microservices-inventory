package io.github.rscai.microservices.inventory.repository;

import io.github.rscai.microservices.inventory.model.InventoryItemQuantityChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemQuantityChangeRepository extends
    JpaRepository<InventoryItemQuantityChange, String> {

}
