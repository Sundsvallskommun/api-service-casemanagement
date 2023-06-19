package se.sundsvall.casemanagement.integration.fb;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import se.sundsvall.casemanagement.integration.fb.configuration.FbConfiguration;
import se.sundsvall.casemanagement.integration.fb.model.ResponseDto;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = FbConfiguration.REGISTRATION_ID, url = "${integration.fb.url}", configuration = FbConfiguration.class)
@CircuitBreaker(name = FbConfiguration.REGISTRATION_ID)
public interface FbClient {

    @PostMapping(path = "/fbservice/Fastighet/info/uuid", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseDto getPropertyInfoByUuid(@RequestBody List<String> registerenheter, @RequestParam(name = "Database") String database,
                                      @RequestParam(name = "User") String user, @RequestParam(name = "Password") String password);

    @PostMapping(path = "/fbservice/adress/search/fastighet/uuid", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseDto getAddressInfoByUuid(@RequestBody List<String> registerenheter, @RequestParam(name = "Database") String database,
                                     @RequestParam("User") String user, @RequestParam("Password") String password);

    @PostMapping(path = "/fbservice/agare/search/lagfarenAgare/fastighet/fnr", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseDto getPropertyOwnerByFnr(@RequestBody List<Integer> fnrList, @RequestParam(name = "Database") String database,
                                      @RequestParam(name = "User") String user, @RequestParam(name = "Password") String password);

    @PostMapping(path = "/fbservice/Agare/inskriven/inskrivenAgareUuid", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseDto getPropertyOwnerInfoByUuid(@RequestBody List<String> uuidList, @RequestParam(name = "Database") String database,
                                           @RequestParam(name = "User") String user, @RequestParam(name = "Password") String password);

    @PostMapping(path = "/fbservice/Agare/adress/personOrganisationsNummer", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseDto getPropertyOwnerAddressByPersOrgNr(@RequestBody List<String> persOrgNrList, @RequestParam(name = "Database") String database,
                                                   @RequestParam(name = "User") String user, @RequestParam(name = "Password") String password);
}
