package io.github.rscai.microservices.inventory.controller;

import io.github.rscai.microservices.inventory.model.InventoryItem;
import io.github.rscai.microservices.inventory.model.InventoryItemQuantityChange;
import io.github.rscai.microservices.inventory.repository.InventoryItemQuantityChangeRepository;
import io.github.rscai.microservices.inventory.repository.InventoryItemRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("inventoryItemQuantityChanges")
@ExposesResourceFor(InventoryItemQuantityChange.class)
public class InventoryItemQuantityChangeController {

  private final EntityLinks entityLinks;
  @Autowired
  private InventoryItemRepository inventoryItemRepository;
  @Autowired
  private InventoryItemQuantityChangeRepository quantityChangeRepository;

  public InventoryItemQuantityChangeController(
      EntityLinks entityLinks) {
    this.entityLinks = entityLinks;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public List<EntityModel<InventoryItemQuantityChange>> create(
      @RequestBody List<InventoryItemQuantityChange> changes) {
    List<EntityModel<InventoryItemQuantityChange>> processedChanges = new ArrayList<>();
    for (final InventoryItemQuantityChange change : changes) {
      if (quantityChangeRepository.existsById(change.getId())) {
        processedChanges.add(new EntityModel<>(change, itemLinks(change)));
        continue;
      }
      InventoryItem item = inventoryItemRepository.findById(change.getInventoryItemId())
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.CONFLICT,
              String.format("Can not find inventory item %s", change.getInventoryItemId())));
      item.setQuantity(item.getQuantity() + change.getQuantityChange());
      inventoryItemRepository.save(item);

      InventoryItemQuantityChange savedChange = quantityChangeRepository.save(change);
      processedChanges.add(new EntityModel<>(savedChange, itemLinks(savedChange)));
    }
    return processedChanges;
  }

  private Link[] itemLinks(final InventoryItemQuantityChange item) {
    return new Link[]{
        entityLinks.linkToItemResource(InventoryItemQuantityChange.class, item.getId())
    };
  }
}
