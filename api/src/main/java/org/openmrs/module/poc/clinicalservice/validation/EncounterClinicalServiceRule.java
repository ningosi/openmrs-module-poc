package org.openmrs.module.poc.clinicalservice.validation;

import java.util.Set;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.poc.api.common.exception.POCBusinessException;
import org.openmrs.module.poc.api.common.validation.IValidationRule;
import org.springframework.stereotype.Component;

@Component
public class EncounterClinicalServiceRule implements IValidationRule<Encounter> {
	
	@Override
	public void validate(final Encounter encounter) throws POCBusinessException {
		
		if ((encounter == null) || (encounter.getUuid() == null)) {
			throw new POCBusinessException("encounter not provided");
		}
		
		final Encounter encounterService = Context.getEncounterService().getEncounterByUuid(encounter.getUuid());
		
		if (encounterService == null) {
			throw new POCBusinessException("encounter not found for given uuid " + encounter.getUuid());
		}
		
		if (encounter.isVoided()) {
			throw new POCBusinessException("Cannot delete services for a voided encounter " + encounter.getUuid());
		}
		
		final Set<Obs> allNonVoidedEncounterObs = encounterService.getAllObs();
		
		if (allNonVoidedEncounterObs.isEmpty()) {
			throw new POCBusinessException("Cannot delete services for encounter without non voided Obs "
			        + encounter.getUuid());
		}
	}
}