package se.sundsvall.casemanagement.service.mapper;

import static se.sundsvall.casemanagement.integration.db.model.DeliveryStatus.PENDING;

import java.sql.Clob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialClob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.integration.db.model.CaseEntity;

public final class CaseMapper {
	private static final Logger LOG = LoggerFactory.getLogger(CaseMapper.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

	private CaseMapper() {
		// Intentionally empty
	}

	public static CaseEntity toCaseEntity(CaseDTO dto) {
		return CaseEntity.builder()
			.withId(dto.getExternalCaseId())
			.withDto(toClob(dto))
			.withDeliveryStatus(PENDING)
			.build();
	}

	private static Clob toClob(CaseDTO dto) {
		try {
			final String jsonString = OBJECT_MAPPER.writeValueAsString(dto);
			return new SerialClob(jsonString.toCharArray());
		} catch (JsonProcessingException | SQLException e) {
			LOG.error("Failed to convert to Clob", e);
			return null;
		}
	}
}
