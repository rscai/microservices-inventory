package io.github.rscai.microservices.inventory.repository;

import io.github.rscai.microservices.inventory.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "inventoryItems", path = "inventoryItems")
public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {
  
}
