package io.github.rscai.microservices.inventory.controller;

import static org.hamcrest.Matchers.closeTo;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rscai.microservices.inventory.RestDocsMockMvcConfiguration;
import io.github.rscai.microservices.inventory.model.InventoryItem;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@Import(RestDocsMockMvcConfiguration.class)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public class InventoryItemTest {

  private static final String ENDPOINT = "/inventoryItems";

  @Autowired
  private MockMvc mvc;
  @Autowired
  private ObjectMapper objectMapper;

  private static LinksSnippet links(LinkDescriptor... descriptors) {
    return HypermediaDocumentation.links(halLinks(), linkWithRel("self").description("self link"),
        linkWithRel("inventoryItem").description("self link")).and(descriptors);
  }

  private static RequestFieldsSnippet requestFields(FieldDescriptor... descriptors) {
    return PayloadDocumentation.requestFields(fieldWithPath("id").ignored(),
        fieldWithPath("productId").type(JsonFieldType.STRING).description("product's id"),
        fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("inventory quantity"),
        fieldWithPath("unitPrice").type(JsonFieldType.NUMBER).description("unit price"),
        fieldWithPath("createdAt").type("Date").description("the timestamp when record created")
            .optional(),
        fieldWithPath("updatedAt").type("Date").description("the timestamp when record updated")
            .optional()).and(descriptors);
  }

  private static ResponseFieldsSnippet responseFields(FieldDescriptor... descriptors) {
    return PayloadDocumentation.responseFields(
        fieldWithPath("productId").type(JsonFieldType.STRING).description("product's id"),
        fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("inventory quantity"),
        fieldWithPath("unitPrice").type(JsonFieldType.NUMBER).description("unit price"),
        fieldWithPath("createdAt").type("Date").description("the timestamp when record created"),
        fieldWithPath("updatedAt").type("Date").description("the timestamp when record updated"),
        subsectionWithPath("_links").description("links to other resources")).and(descriptors);
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
        post(ENDPOINT).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))).andDo(print())
        .andExpect(status().isCreated()).andExpect(jsonPath("$.productId", is(productId)))
        .andExpect(jsonPath("$.quantity", is(quantity)))
        .andExpect(jsonPath("$.unitPrice", is(closeTo(unitPrice.doubleValue(), 0.0001))))
        .andExpect(jsonPath("$.createdAt", notNullValue()))
        .andExpect(jsonPath("$.updatedAt", notNullValue()))
        .andDo(document("inventoryItem/create", links(), requestFields(), responseFields()))
        .andReturn().getResponse().getContentAsString();

    String newId = Stream
        .of(objectMapper.readTree(createResponse).at("/_links/self/href").asText().split("/"))
        .reduce((first, second) -> second).orElse(null);

    mvc.perform(get(ENDPOINT + "/{id}", newId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(jsonPath("$.productId", is(productId)))
        .andExpect(jsonPath("$.quantity", is(quantity)))
        .andExpect(jsonPath("$.unitPrice", is(closeTo(unitPrice.doubleValue(), 0.0001))))
        .andExpect(jsonPath("$.createdAt", notNullValue()))
        .andExpect(jsonPath("$.updatedAt", notNullValue())).andDo(
        document("inventoryItem/getOne", links(),
            pathParameters(parameterWithName("id").description("inventory item's id")),
            responseFields()));
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
        post(ENDPOINT).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
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
        .andExpect(status().is2xxSuccessful()).andDo(document("inventoryItem/update", links(),
        pathParameters(parameterWithName("id").description("inventory item's id")), requestFields(),
        responseFields()));

    mvc.perform(get(ENDPOINT + "/{id}", newId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(jsonPath("$.productId", is(productId)))
        .andExpect(jsonPath("$.quantity", is(newQuantity)))
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
        post(ENDPOINT).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))).andDo(print())
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    String newId = Stream
        .of(objectMapper.readTree(createResponse).at("/_links/self/href").asText().split("/"))
        .reduce((first, second) -> second).orElse(null);

    mvc.perform(delete(ENDPOINT + "/{id}", newId)).andExpect(status().is2xxSuccessful()).andDo(
        document("inventoryItem/delete",
            pathParameters(parameterWithName("id").description("inventory item's id"))));

    mvc.perform(get(ENDPOINT + "/{id}", newId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
