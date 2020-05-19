package io.github.rscai.microservices.inventory.controller;

import io.github.rscai.microservices.inventory.model.InventoryItem;
import io.github.rscai.microservices.inventory.repository.InventoryItemRepository;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("inventoryItems")
@ExposesResourceFor(
    InventoryItem.class)
@PreAuthorize("hasAuthority('SCOPE_inventory.read')")
public class InventoryItemController {

  private static final String AUTHORITY_INVENTORY_WRITE = "hasAuthority('SCOPE_inventory.write')";

  private final EntityLinks entityLinks;
  @Autowired
  private PagedResourcesAssembler<InventoryItem> pagedResourcesAssembler;
  @Autowired
  private InventoryItemRepository repository;

  public InventoryItemController(EntityLinks entityLinks) {
    this.entityLinks = entityLinks;
  }

  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(AUTHORITY_INVENTORY_WRITE)
  public EntityModel<InventoryItem> create(@RequestBody InventoryItem entity) {
    InventoryItem createdOne = repository.save(entity);
    return new EntityModel<>(createdOne, itemLinks(createdOne));
  }

  @PutMapping(value = "{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(AUTHORITY_INVENTORY_WRITE)
  public void update(@PathVariable("id") String id,
      @RequestBody InventoryItem entity) {
    InventoryItem existedOne = repository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    existedOne.setProductId(entity.getProductId());
    existedOne.setUnitPrice(entity.getUnitPrice());
    repository.save(existedOne);
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(AUTHORITY_INVENTORY_WRITE)
  public void delete(@PathVariable("id") String id) {
    InventoryItem existedOne = repository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    repository.delete(existedOne);
  }

  @GetMapping(value = "{id}", produces = "application/hal+json")
  public EntityModel<InventoryItem> getOne(@PathVariable("id") String id) {
    InventoryItem existedOne = repository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return new EntityModel<>(existedOne, itemLinks(existedOne));
  }

  @GetMapping(value = "search/productIdIn", produces = "application/hal+json")
  public PagedModel<EntityModel<InventoryItem>> searchByProductId(
      @RequestParam("productId") List<String> productIds, @NotNull Pageable pageable) {
    Page<InventoryItem> pagedItems = repository.findByProductIdIn(productIds, pageable);
    return pagedResourcesAssembler.toModel(pagedItems);
  }

  private Link[] itemLinks(final InventoryItem item) {
    return new Link[]{
        entityLinks.linkToItemResource(InventoryItem.class, item.getId())
    };
  }
}
