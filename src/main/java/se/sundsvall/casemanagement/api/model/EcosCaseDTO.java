package se.sundsvall.casemanagement.api.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import se.sundsvall.casemanagement.api.validation.EcosCaseDateOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@EcosCaseDateOrder
@SuperBuilder(setterPrefix = "with")
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ecos2-case model class")
public class EcosCaseDTO extends CaseDTO implements Serializable {

	@Schema(description = "Start date for the business.", format = "date", example = "2022-01-01")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@FutureOrPresent
	@Schema(description = "End date of the business if it is time-limited.", format = "date", example = "2022-06-01")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	@NotEmpty
	@Size(min = 1, max = 1, message = "size must be 1")
	@Valid
	@Schema(description = "The facilities in the case")
	private List<@Valid FacilityDTO> facilities;

}
