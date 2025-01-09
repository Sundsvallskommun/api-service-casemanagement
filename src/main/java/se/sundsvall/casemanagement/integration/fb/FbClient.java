package se.sundsvall.casemanagement.integration.fb;

import static se.sundsvall.casemanagement.integration.fb.configuration.FbConfiguration.REGISTRATION_ID;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.casemanagement.integration.fb.configuration.FbConfiguration;
import se.sundsvall.casemanagement.integration.fb.model.ResponseDto;

@FeignClient(name = REGISTRATION_ID, url = "${integration.fb.url}", configuration = FbConfiguration.class)
@CircuitBreaker(name = REGISTRATION_ID)
public interface FbClient {

	@PostMapping(path = "/fbservice/Fastighet/info/uuid")
	ResponseDto getPropertyInfoByUuid(@RequestBody List<String> registerenheter, @RequestParam(name = "Database") String database,
		@RequestParam(name = "User") String user, @RequestParam(name = "Password") String password);

	@PostMapping(path = "/fbservice/adress/search/fastighet/uuid")
	ResponseDto getAddressInfoByUuid(@RequestBody List<String> registerenheter, @RequestParam(name = "Database") String database,
		@RequestParam("User") String user, @RequestParam("Password") String password);

	@PostMapping(path = "/fbservice/agare/search/lagfarenAgare/fastighet/fnr")
	ResponseDto getPropertyOwnerByFnr(@RequestBody List<Integer> fnrList, @RequestParam(name = "Database") String database,
		@RequestParam(name = "User") String user, @RequestParam(name = "Password") String password);

	@PostMapping(path = "/fbservice/Agare/inskriven/inskrivenAgareUuid")
	ResponseDto getPropertyOwnerInfoByUuid(@RequestBody List<String> uuidList, @RequestParam(name = "Database") String database,
		@RequestParam(name = "User") String user, @RequestParam(name = "Password") String password);

	@PostMapping(path = "/fbservice/Agare/adress/personOrganisationsNummer")
	ResponseDto getPropertyOwnerAddressByPersOrgNr(@RequestBody List<String> persOrgNrList, @RequestParam(name = "Database") String database,
		@RequestParam(name = "User") String user, @RequestParam(name = "Password") String password);
}
