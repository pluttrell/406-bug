package example.bug;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
class AccessKeyValidationService {

  Mono<Boolean> isValid(String accessKey) {
    return Mono.defer(() -> Mono.just(blockingLookup(accessKey)))
        .subscribeOn(Schedulers.elastic());
  }

  private Boolean blockingLookup(String accessKey) {
    //Some blocking lookup...
    return accessKey.equals("good");
  }

}