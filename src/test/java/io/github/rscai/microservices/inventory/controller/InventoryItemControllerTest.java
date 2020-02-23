package io.github.rscai.microservices.inventory.controller;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.halLinks;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rscai.microservices.inventory.RestDocsMockMvcConfiguration;
import io.github.rscai.microservices.inventory.model.InventoryItem;
import io.github.rscai.microservices.inventory.repository.InventoryItemRepository;
import java.math.BigDecimal;
import java.util.stream.Stream;
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
import org.springframework.restdocs.hypermedia.HypermediaDocumentation;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.RequestParametersSnippet;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@Import(RestDocsMockMvcConfiguration.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public class InventoryItemControllerTest {

  private static final String ENDPOINT = "/inventoryItems";
  private static final String APPLICATION_HAL = "application/hal+json";
  private static final String PRODUCT_ID_A = "productA";
  private static final String PRODUCT_ID_B = "productB";
  private static final String PRODUCT_ID_C = "productC";

  @Autowired
  private InventoryItemRepository inventoryItemRepository;
  @Autowired
  private MockMvc mvc;
  @Autowired
  private ObjectMapper objectMapper;

  private String itemIdA;
  private String itemIdB;
  private String itemIdC;


  @BeforeEach
  public void setup() {
    InventoryItem itemA = new InventoryItem();
    itemA.setProductId(PRODUCT_ID_A);
    itemA.setUnitPrice(BigDecimal.valueOf(123.45));
    itemA.setQuantity(100);
    itemIdA = inventoryItemRepository.save(itemA).getId();

    InventoryItem itemB = new InventoryItem();
    itemB.setProductId(PRODUCT_ID_B);
    itemB.setUnitPrice(BigDecimal.valueOf(456.78));
    itemB.setQuantity(200);
    itemIdB = inventoryItemRepository.save(itemB).getId();

    InventoryItem itemC = new InventoryItem();
    itemC.setProductId(PRODUCT_ID_C);
    itemC.setUnitPrice(BigDecimal.valueOf(789.01));
    itemC.setQuantity(300);
    itemIdC = inventoryItemRepository.save(itemC).getId();
  }

  @AfterEach
  public void tearDown() {
    inventoryItemRepository.deleteAll();
  }

  @Test
  public void testCreateAndGet() throws Exception {
    final String productId = "P123456";
    final int quantity = 12;
    final BigDecimal unitPrice = BigDecimal.valueOf(12.12);

    final InventoryItem input = new InventoryItem();
    input.setProductId(productId);
    input.setQuantity(quantity);
    input.setUnitPrice(unitPrice);

    String createResponse = mvc.perform(
        post(ENDPOINT).accept(MediaType.APPLICATION_JSON_VALUE, APPLICATION_HAL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))).andDo(print())
        .andExpect(status().isCreated()).andExpect(jsonPath("$.productId", is(productId)))
        .andExpect(jsonPath("$.quantity", is(quantity)))
        .andExpect(jsonPath("$.unitPrice", is(closeTo(unitPrice.doubleValue(), 0.0001))))
        .andExpect(jsonPath("$.createdAt", notNullValue()))
        .andExpect(jsonPath("$.updatedAt", notNullValue()))
        .andDo(
            document("inventoryItem/create", itemLinks(), itemRequestFields(), itemResponseField()))
        .andReturn().getResponse().getContentAsString();

    String newId = Stream
        .of(objectMapper.readTree(createResponse).at("/_links/self/href").asText().split("/"))
        .reduce((first, second) -> second).orElse(null);

    mvc.perform(
        get(ENDPOINT + "/{id}", newId).accept(MediaType.APPLICATION_JSON_VALUE, APPLICATION_HAL))
        .andExpect(status().isOk()).andExpect(jsonPath("$.productId", is(productId)))
        .andExpect(jsonPath("$.quantity", is(quantity)))
        .andExpect(jsonPath("$.unitPrice", is(closeTo(unitPrice.doubleValue(), 0.0001))))
        .andExpect(jsonPath("$.createdAt", notNullValue()))
        .andExpect(jsonPath("$.updatedAt", notNullValue())).andDo(
        document("inventoryItem/getOne", itemLinks(),
            pathParameters(parameterWithName("id").description("inventory item's id")),
            itemResponseField()));
  }

  @Test
  public void testUpdate() throws Exception {
    final String productId = "P123456";
    final int quantity = 12;
    final BigDecimal unitPrice = BigDecimal.valueOf(12.12);

    final InventoryItem input = new InventoryItem();
    input.setProductId(productId);
    input.setQuantity(quantity);
    input.setUnitPrice(unitPrice);

    String createResponse = mvc.perform(
        post(ENDPOINT).accept(MediaType.APPLICATION_JSON_VALUE, APPLICATION_HAL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))).andDo(print())
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    String newId = Stream
        .of(objectMapper.readTree(createResponse).at("/_links/self/href").asText().split("/"))
        .reduce((first, second) -> second).orElse(null);

    final int newQuantity = 45;
    final BigDecimal newUnitPrice = BigDecimal.valueOf(98.45);

    input.setQuantity(newQuantity);
    input.setUnitPrice(newUnitPrice);

    mvc.perform(put(ENDPOINT + "/{id}", newId).accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(input)))
        .andExpect(status().isNoContent()).andDo(document("inventoryItem/update",
        pathParameters(parameterWithName("id").description("inventory item's id")),
        itemRequestFields()));

    mvc.perform(
        get(ENDPOINT + "/{id}", newId).accept(MediaType.APPLICATION_JSON_VALUE, APPLICATION_HAL))
        .andExpect(status().isOk()).andExpect(jsonPath("$.productId", is(productId)))
        .andExpect(jsonPath("$.unitPrice", is(closeTo(newUnitPrice.doubleValue(), 0.0001))))
        .andExpect(jsonPath("$.createdAt", notNullValue()))
        .andExpect(jsonPath("$.updatedAt", notNullValue()));


  }

  @Test
  public void testDelete() throws Exception {
    final String productId = "P123456";
    final int quantity = 12;
    final BigDecimal unitPrice = BigDecimal.valueOf(12.12);

    final InventoryItem input = new InventoryItem();
    input.setProductId(productId);
    input.setQuantity(quantity);
    input.setUnitPrice(unitPrice);

    String createResponse = mvc.perform(
        post(ENDPOINT).accept(MediaType.APPLICATION_JSON_VALUE, APPLICATION_HAL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))).andDo(print())
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    String newId = Stream
        .of(objectMapper.readTree(createResponse).at("/_links/self/href").asText().split("/"))
        .reduce((first, second) -> second).orElse(null);

    mvc.perform(delete(ENDPOINT + "/{id}", newId)).andExpect(status().isNoContent()).andDo(
        document("inventoryItem/delete",
            pathParameters(parameterWithName("id").description("inventory item's id"))));

    mvc.perform(get(ENDPOINT + "/{id}", newId))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testSearchByProductIdIn() throws Exception {
    mvc.perform(get(ENDPOINT
            + "/search/productIdIn?productId={productId1}&productId={productId2}&page={page}&size={size}&sort={sort}",
        PRODUCT_ID_A, PRODUCT_ID_B, 0, 2, "productId,ASC")
        .accept(APPLICATION_HAL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.inventoryItems", hasSize(2)))
        .andExpect(jsonPath("$._embedded.inventoryItems[0].productId", is(PRODUCT_ID_A)))
        .andExpect(jsonPath("$._embedded.inventoryItems[1].productId", is(PRODUCT_ID_B)))
        .andDo(document("inventory/search/productId", pageRequestParameters(
            parameterWithName("productId").description("product's unique identifier")), pageLinks(),
            pageResponseFields()));
  }

  private static RequestParametersSnippet pageRequestParameters(
      ParameterDescriptor... descriptors) {
    return requestParameters(parameterWithName("page").description("0-based page index"),
        parameterWithName("size").description("page size"),
        parameterWithName("sort").description("sort expression")).and(descriptors);
  }

  private static LinksSnippet itemLinks(LinkDescriptor... descriptors) {
    return HypermediaDocumentation.links(halLinks(), linkWithRel("self").description("self link"))
        .and(descriptors);
  }

  private static RequestFieldsSnippet itemRequestFields(FieldDescriptor... descriptors) {
    return PayloadDocumentation.requestFields(fieldWithPath("id").ignored(),
        fieldWithPath("productId").type(JsonFieldType.STRING).description("product's id"),
        fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("inventory quantity"),
        fieldWithPath("unitPrice").type(JsonFieldType.NUMBER).description("unit price"),
        fieldWithPath("createdAt").type("Date").description("the timestamp when record created")
            .optional(),
        fieldWithPath("updatedAt").type("Date").description("the timestamp when record updated")
            .optional()).and(descriptors);
  }

  private static ResponseFieldsSnippet itemResponseField(FieldDescriptor... descriptors) {
    return PayloadDocumentation.responseFields(
        fieldWithPath("id").optional().type(JsonFieldType.STRING).description("unique identifier"),
        fieldWithPath("productId").type(JsonFieldType.STRING).description("product's id"),
        fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("inventory quantity"),
        fieldWithPath("unitPrice").type(JsonFieldType.NUMBER).description("unit price"),
        fieldWithPath("createdAt").type("Date").description("the timestamp when record created"),
        fieldWithPath("updatedAt").type("Date").description("the timestamp when record updated"),
        subsectionWithPath("_links").description("links to other resources")).and(descriptors);
  }


  private LinksSnippet pageLinks(LinkDescriptor... descriptors) {
    return HypermediaDocumentation.links(halLinks(),
        linkWithRel("self").description("self link"))
        .and(descriptors);
  }

  private ResponseFieldsSnippet pageResponseFields(FieldDescriptor... descriptors) {
    return PayloadDocumentation
        .responseFields(subsectionWithPath("_links").description("links to other resources"),
            subsectionWithPath("_embedded.inventoryItems").type(JsonFieldType.ARRAY)
                .description("inventoryItem collection"),
            fieldWithPath("page.size").type(JsonFieldType.NUMBER).description("page size"),
            fieldWithPath("page.number").type(JsonFieldType.NUMBER)
                .description("0-based page index"),
            fieldWithPath("page.totalElements").type(JsonFieldType.NUMBER)
                .description("the count of items which matched the search criteria"),
            fieldWithPath("page.totalPages").type(JsonFieldType.NUMBER)
                .description("the count of pages"))
        .and(descriptors);
  }
}
