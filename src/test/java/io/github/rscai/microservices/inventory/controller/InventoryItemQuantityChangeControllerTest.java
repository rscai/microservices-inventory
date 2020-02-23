package io.github.rscai.microservices.inventory.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rscai.microservices.inventory.RestDocsMockMvcConfiguration;
import io.github.rscai.microservices.inventory.model.InventoryItem;
import io.github.rscai.microservices.inventory.model.InventoryItemQuantityChange;
import io.github.rscai.microservices.inventory.repository.InventoryItemQuantityChangeRepository;
import io.github.rscai.microservices.inventory.repository.InventoryItemRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import javax.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@Import(RestDocsMockMvcConfiguration.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
public class InventoryItemQuantityChangeControllerTest {

  private static final String COLLECTION_ENDPOINT = "/inventoryItemQuantityChanges";
  private static final String APPLICATION_HAL = "application/hal+json";
  private static final String PRODUCT_ID_A = "productA";
  private static final String PRODUCT_ID_B = "productB";
  private static final String PRODUCT_ID_C = "productC";

  @Autowired
  private MockMvc mvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private InventoryItemRepository itemRepository;
  @Autowired
  private InventoryItemQuantityChangeRepository quantityChangeRepository;

  private String itemIdA;
  private String itemIdB;
  private String itemIdC;

  @BeforeEach
  public void setUp() {
    InventoryItem itemA = new InventoryItem();
    itemA.setProductId(PRODUCT_ID_A);
    itemA.setUnitPrice(BigDecimal.valueOf(123.45));
    itemA.setQuantity(100);
    itemIdA = itemRepository.save(itemA).getId();

    InventoryItem itemB = new InventoryItem();
    itemB.setProductId(PRODUCT_ID_B);
    itemB.setUnitPrice(BigDecimal.valueOf(456.78));
    itemB.setQuantity(200);
    itemIdB = itemRepository.save(itemB).getId();

    InventoryItem itemC = new InventoryItem();
    itemC.setProductId(PRODUCT_ID_C);
    itemC.setUnitPrice(BigDecimal.valueOf(789.01));
    itemC.setQuantity(300);
    itemIdC = itemRepository.save(itemC).getId();
  }

  @AfterEach
  public void tearDown() {
    itemRepository.deleteAll();
    quantityChangeRepository.deleteAll();
  }

  @Test
  public void testCreateQuantityChange() throws Exception {
    InventoryItemQuantityChange changeA = new InventoryItemQuantityChange(
        String.format("inventorytest-order12345-%s", itemIdA), itemIdA, -10, new Date());
    InventoryItemQuantityChange changeB = new InventoryItemQuantityChange(
        String.format("inventorytest-order12345-%s", itemIdB), itemIdB, -20, new Date());
    InventoryItemQuantityChange changeC = new InventoryItemQuantityChange(
        String.format("inventorytest-order12345-%s", itemIdC), itemIdC, -30, new Date());

    mvc.perform(
        post(COLLECTION_ENDPOINT).accept(APPLICATION_HAL).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                Arrays.asList(changeA, changeB))))
        .andExpect(status().isCreated())
        .andDo(document("inventoryItemQuantityChange/create"));

    assertThat(
        itemRepository.findById(itemIdA)
            .orElseThrow(() -> new AssertionError("inventoryItem not found")).getQuantity(),
        is(90));
    assertThat(
        itemRepository.findById(itemIdB)
            .orElseThrow(() -> new AssertionError("inventoryItem not found")).getQuantity(),
        is(180));
    assertThat(
        itemRepository.findById(itemIdC)
            .orElseThrow(() -> new AssertionError("inventoryItem not found")).getQuantity(),
        is(300));
    mvc.perform(
        post(COLLECTION_ENDPOINT).accept(APPLICATION_HAL).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                Arrays.asList(changeA, changeB, changeC))))
        .andExpect(status().isCreated())
        .andDo(document("inventoryItemQuantityChange/create"));

    assertThat(
        itemRepository.findById(itemIdA)
            .orElseThrow(() -> new AssertionError("inventoryItem not found")).getQuantity(),
        is(90));
    assertThat(
        itemRepository.findById(itemIdB)
            .orElseThrow(() -> new AssertionError("inventoryItem not found")).getQuantity(),
        is(180));
    assertThat(
        itemRepository.findById(itemIdC)
            .orElseThrow(() -> new AssertionError("inventoryItem not found")).getQuantity(),
        is(270));
  }
}
