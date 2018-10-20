# 406-bug

This project demonstrates a bug in SpringBoot v2.1.0.M4 that didn't exist in v2.1.0.RC1.

The following included test passes with both v2.1.0.RC1 and v2.1.0.M4:

```
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
```

If you make the call manually with v2.1.0.M4 it works as such:

```
$ http localhost:8080/binary-test apiKey==not-valid
HTTP/1.1 403 Forbidden
Content-Length: 36
Content-Type: application/json;charset=UTF-8

{
    "developerMessage": "access denied"
}
```

But if you make the call manually with v2.1.0.RC1 it fails with a 406:

```
$ http localhost:8080/binary-test apiKey==not-valid                                                                                                              
HTTP/1.1 406 Not Acceptable
Content-Length: 157
Content-Type: application/json;charset=UTF-8

{
    "error": "Not Acceptable",
    "message": "Could not find acceptable representation",
    "path": "/binary-test",
    "status": 406,
    "timestamp": "2018-10-20T08:31:18.092+0000"
}
```
