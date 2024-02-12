package se.sundsvall.casemanagement.integration.ecos;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.util.CaseUtil;

import minutmiljo.AddFacilityToCase;
import minutmiljo.ArrayOfFacilityFilterSvcDto;
import minutmiljo.ArrayOfSaveRiskClass2024ActivityDto;
import minutmiljo.ArrayOfSaveRiskClass2024CertificationDto;
import minutmiljo.ArrayOfSaveRiskClass2024ProductGroupDto;
import minutmiljo.ArrayOfguid;
import minutmiljo.FacilityFacilityStatusIdsFilterSvcDto;
import minutmiljo.FacilityFacilityTypeIdsFilterSvcDto;
import minutmiljo.FacilityNotFilterSvcDto;
import minutmiljo.FacilityPartyOrganizationNumberFilterSvcDto;
import minutmiljo.SaveFoodFacility2024RiskClassData;
import minutmiljo.SaveRiskClass2024ActivityDto;
import minutmiljo.SaveRiskClass2024CertificationDto;
import minutmiljo.SaveRiskClass2024DataDto;
import minutmiljo.SaveRiskClass2024ProductGroupDto;
import minutmiljo.SearchFacility;
import minutmiljo.SearchFacilitySvcDto;

@Service
public class RiskClassService {

	private static final String MAIN_ORIENTATION_ID = "MainOrientationId";

	private static final String PROD_SIZE_ID = "ProductionSizeId";

	private static final String IS_SEASONAL = "IsSeasonal";

	private static final String SEASONAL_NOTE = "seasonalNote";

	//Since there is too many activities for one extra parameter (on the openE side)
	// we need to split it up in different extra parameters.
	private static final List<String> ACTIVITIES = List.of("activities", "activities2", "activities3", "activities4");

	private static final String PRODUCT_GROUPS = "productGroups";

	private static final String THIRD_PARTY_CERTS = "thirdPartyCertifications";

	private static final String FACILITY_TYPE_ID = "4958BC00-76E8-4D5B-A862-AAF8E815202A"; //Livsmedelsanläggning

	private static final String FACILITY_STATUS_ID_APPLIED = "88E11CAA-DF35-4C5E-94A8-3C7B0369D8F2"; //Anmäld/Ansökt

	private static final String FACILITY_STATUS_ID_INACTIVE = "64B2DB7A-9A11-4F20-A57C-8122B1A469E6"; // Inaktiv

	private static final String FACILITY_STATUS_ID_ACTIVE = "D203BB33-EB9A-4679-8E1C-BBD8AF86E554"; //Aktiv

	private static final String FACILITY_STATUS_ID_GRANTED = "C5A98B2B-C2B8-428E-B597-A3F97A77B818"; ///Beviljad

	private static final String FACILITY_STATUS_ID_REVOKED = "9A748E4E-BD7E-481A-B449-73CBD0992213"; //Upphörd/Skrotad

	private static final String FACILITY_STATUS_ID_DISCARDED = "80FFA45C-B3DF-4A10-8DB3-A042F36C64B7"; //Makulerad

	private final MinutMiljoClient minutMiljoClient;

	public RiskClassService(final MinutMiljoClient minutMiljoClient) {
		this.minutMiljoClient = minutMiljoClient;
	}

	public void updateRiskClass(final EnvironmentalCaseDTO caseInput, final String caseId) {
		final var facilityId = searchFacility(extractOrgNr(caseInput), caseInput.getFacilities().getFirst().getFacilityCollectionName());
		addFacilityToCase(facilityId, caseId);
		final var data = createSaveRiskClassObject(facilityId, caseId, caseInput);
		minutMiljoClient.updateRiskClass(data);
	}

	private String extractOrgNr(final EnvironmentalCaseDTO eCase) {
		return CaseUtil.getSokigoFormattedOrganizationNumber(eCase.getStakeholders().stream()
			.map(stakeholderDTO -> {
				if (stakeholderDTO instanceof final OrganizationDTO orgDTO) {
					return orgDTO.getOrganizationNumber();
				}
				return "";
			})
			.findFirst()
			.orElse(""));
	}

	private String searchFacility(final String orgNr, final String facilityName) {


		final var facilityTypeFilter = new FacilityFacilityTypeIdsFilterSvcDto()
			.withFacilityTypeIds(FACILITY_TYPE_ID);

		final var facilityStatusFilter =
			new FacilityFacilityStatusIdsFilterSvcDto()
				.withFacilityStatusIds(new ArrayOfguid().withGuid(
					FACILITY_STATUS_ID_APPLIED,
					FACILITY_STATUS_ID_INACTIVE,
					FACILITY_STATUS_ID_ACTIVE,
					FACILITY_STATUS_ID_GRANTED
				));

		final var notFacilityStatusFilters = new FacilityNotFilterSvcDto()
			.withFilter(new FacilityFacilityStatusIdsFilterSvcDto()
				.withFacilityStatusIds(new ArrayOfguid()
					.withGuid(List.of(FACILITY_STATUS_ID_REVOKED,
						FACILITY_STATUS_ID_DISCARDED))));

		final var orgFilter = new FacilityPartyOrganizationNumberFilterSvcDto()
			.withOrganizationNumber(orgNr);

		final var result = Optional.ofNullable(minutMiljoClient
				.searchFacility(new SearchFacility().withSearchFacilitySvcDto(new SearchFacilitySvcDto()
					.withFacilityFilters(new ArrayOfFacilityFilterSvcDto()
						.withFacilityFilterSvcDto(facilityStatusFilter, facilityTypeFilter, notFacilityStatusFilters, orgFilter))))
				.getSearchFacilityResult()
				.getSearchFacilityResultSvcDto())
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "Could not find facility "));

		return result.stream()
			.filter(resultSvcDto -> resultSvcDto.getFacilityName() != null)
			.filter(resultSvcDto -> resultSvcDto.getFacilityName().trim().equalsIgnoreCase(facilityName.trim()))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(Status.BAD_REQUEST,
				("Could not match facilityName: %s to a facility belonging to organization with organizationNumber: %s")
					.formatted(facilityName, orgNr)))
			.getFacilityId();
	}

	private void addFacilityToCase(final String facilityId, final String caseId) {
		minutMiljoClient.addFacilityToCase(new AddFacilityToCase()
			.withFacilityId(facilityId)
			.withCaseId(caseId));

	}

	private SaveFoodFacility2024RiskClassData createSaveRiskClassObject(final String facilityId,
		final String caseId, final EnvironmentalCaseDTO dto) {

		return new SaveFoodFacility2024RiskClassData()
			.withModel(new SaveRiskClass2024DataDto()
				.withCaseId(caseId)
				.withFacilityId(facilityId)
				.withMainOrientationSlvCode(dto.getExtraParameters().get(MAIN_ORIENTATION_ID))
				.withProductionSizeSlvCode(dto.getExtraParameters().get(PROD_SIZE_ID))
				.withIsSeasonal("true".equalsIgnoreCase(Optional.ofNullable(dto.getExtraParameters().get(IS_SEASONAL)).orElse("")))
				.withSeasonalNote(dto.getExtraParameters().get(SEASONAL_NOTE))
				.withActivities(mapActivities(dto))
				.withProductGroups(mapProductGroups(dto.getExtraParameters().get(PRODUCT_GROUPS)))
				.withThirdPartyCertifications(mapThirdPartyCertifications(dto.getExtraParameters().get(THIRD_PARTY_CERTS))));
	}

	private ArrayOfSaveRiskClass2024ActivityDto mapActivities(final EnvironmentalCaseDTO dto) {
		final StringBuilder activityString = new StringBuilder();

		for (final var activityParam : ACTIVITIES) {

			activityString.append(Optional.ofNullable(dto.getExtraParameters().get(activityParam)).orElse(" ")).append(
				",");
		}

		if (activityString.isEmpty()) {
			return null;
		}
		final var activities = splitString(activityString.toString());

		return new ArrayOfSaveRiskClass2024ActivityDto()
			.withSaveRiskClass2024ActivityDto(activities.stream()
				.map(activitySlv -> new SaveRiskClass2024ActivityDto()
					.withSlvCode(activitySlv))
				.filter(activityDto -> !activityDto.getSlvCode().isEmpty())
				.toList());
	}

	private ArrayOfSaveRiskClass2024ProductGroupDto mapProductGroups(final String productGroupIdString) {
		if (productGroupIdString == null) {
			return null;
		}

		final var productGroupIds = splitString(productGroupIdString);

		if (productGroupIds.getFirst().isEmpty()) {
			return null;
		}
		return new ArrayOfSaveRiskClass2024ProductGroupDto()
			.withSaveRiskClass2024ProductGroupDto(productGroupIds.stream()
				.map(productGroupId -> new SaveRiskClass2024ProductGroupDto()
					.withSlvCode(productGroupId))
				.toList());
	}

	private ArrayOfSaveRiskClass2024CertificationDto mapThirdPartyCertifications(final String thirdPartyCertString) {
		if (thirdPartyCertString == null) {
			return new ArrayOfSaveRiskClass2024CertificationDto().withSaveRiskClass2024CertificationDto(new SaveRiskClass2024CertificationDto());
		}
		final var dtos = splitString(thirdPartyCertString);

		return new ArrayOfSaveRiskClass2024CertificationDto()
			.withSaveRiskClass2024CertificationDto(
				dtos.stream()
					.map(dto -> new SaveRiskClass2024CertificationDto()
						.withThirdPartyCertificationText(dto))
					.toList());
	}

	private List<String> splitString(final String string) {
		return Arrays.asList(string.split("(,+\\s?)"));
	}

}
