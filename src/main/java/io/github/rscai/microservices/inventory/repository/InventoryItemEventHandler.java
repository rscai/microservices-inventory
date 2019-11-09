package io.github.rscai.microservices.inventory.repository;

import io.github.rscai.microservices.inventory.model.InventoryItem;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler(InventoryItem.class)
public class InventoryItemEventHandler {

  @Autowired
  private InventoryItemRepository entityRepository;

  @HandleBeforeCreate
  public void handleBeforeCreate(InventoryItem entity) {
    if (entity.getCreatedAt() == null) {
      entity.setCreatedAt(new Date());
    }
    entity.setUpdatedAt(new Date());
  }

  @HandleBeforeSave
  public void handleBeforeSave(InventoryItem entity) {
    final Optional<InventoryItem> existedEntity = entityRepository.findById(entity.getId());
    if (existedEntity.isPresent()) {
      entity.setCreatedAt(existedEntity.get().getCreatedAt());
    }
    entity.setUpdatedAt(new Date());
  }
}
