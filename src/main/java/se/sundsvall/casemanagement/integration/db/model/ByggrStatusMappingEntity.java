package se.sundsvall.casemanagement.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "byggr_status_mapping")
@Builder(setterPrefix = "with")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ByggrStatusMappingEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "handelse_typ")
	private String handelseTyp;

	@Column(name = "handelse_slag")
	private String handelseSlag;

	@Column(name = "handelse_utfall")
	private String handelseUtfall;

	@Column(name = "return_field", nullable = false)
	private String returnField;

}
