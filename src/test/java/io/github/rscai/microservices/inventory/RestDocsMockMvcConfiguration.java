package io.github.rscai.microservices.inventory;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;

@TestConfiguration
public class RestDocsMockMvcConfiguration implements
    RestDocsMockMvcConfigurationCustomizer {

  @Override
  public void customize(MockMvcRestDocumentationConfigurer configurer) {
    configurer.operationPreprocessors().withRequestDefaults(prettyPrint())
        .withResponseDefaults(prettyPrint());
  }
}
