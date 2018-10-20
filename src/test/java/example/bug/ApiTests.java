package example.bug;

import static org.mockito.BDDMockito.given;

import java.net.URI;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ApiTests {

  @Autowired
  protected WebTestClient webTestClient;

  @SpyBean
  protected AccessKeyValidationService accessKeyValidationService;

  @Test
  void testParamWhenApiKeyValid() {

    String apiKey = "valid";

    given(accessKeyValidationService.isValid(apiKey)).willReturn(Mono.just(Boolean.TRUE));

    webTestClient.get()
        .uri(uriBuilder -> buildUrl(uriBuilder, apiKey, false))
        .exchange()
        .expectStatus().isOk();

  }

  @Test
  void testParamWhenApiKeyNotValid() {

    String apiKey = "not-valid";

    given(accessKeyValidationService.isValid(apiKey)).willReturn(Mono.just(Boolean.FALSE));

    webTestClient.get()
        .uri(uriBuilder -> buildUrl(uriBuilder, apiKey, false))
        .exchange()
        .expectStatus().isForbidden()
        .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
        .expectBody()
        .jsonPath("$.developerMessage").isEqualTo("access denied");

  }

  @Test
  void testParamFailFastWhenFailing() {

    String apiKey = "not-valid";

    webTestClient.get()
        .uri(uriBuilder -> buildUrl(uriBuilder, apiKey, true))
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
        .expectBody()
        .jsonPath("$.developerMessage").isEqualTo("better luck next time");

  }

  private URI buildUrl(UriBuilder uriBuilder, String apiKey, boolean failFirst) {

    uriBuilder = uriBuilder.path("/binary-test");

    if (!Strings.isNullOrEmpty(apiKey)) {
      uriBuilder = uriBuilder.queryParam("apiKey", apiKey);
    }

    if (failFirst) {
      uriBuilder = uriBuilder.queryParam("failFirst", true);
    }

    return uriBuilder.build();

  }

}
