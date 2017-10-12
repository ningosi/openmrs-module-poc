package org.openmrs.module.poc.clinicalservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.poc.api.common.exception.POCBusinessException;
import org.openmrs.module.poc.clinicalservice.util.ClinicalServiceKeys;
import org.openmrs.module.poc.clinicalservice.util.MappedClinicalServices;
import org.openmrs.module.poc.clinicalservice.validation.ClinicalServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;

public class ClinicalServiceServiceImpl extends BaseOpenmrsService implements ClinicalServiceService {
	
	private ObsService obsService;
	
	private ConceptService conceptService;
	
	private EncounterService encounterService;
	
	@Autowired
	private ClinicalServiceValidator clinicalServiceValidator;
	
	@Override
	public void deleteClinicalService(final String encounterUuid, final String clinicalServiceKey)
	        throws POCBusinessException {
		
		final Encounter es = new Encounter();
		es.setUuid(encounterUuid);
		
		this.clinicalServiceValidator.validateDeletion(es, clinicalServiceKey);
		
		final Encounter encounter = this.encounterService.getEncounterByUuid(encounterUuid);
		final List<Concept> clinicalServices = this.getClinicalServices(ClinicalServiceKeys.valueOf(clinicalServiceKey));
		final Set<Obs> allObs = encounter.getAllObs();
		
		for (final Obs obs : allObs) {
			
			final Concept obsClinicalService = obs.getConcept();
			
			if (clinicalServices.contains(obsClinicalService)) {
				if (!obs.isVoided()) {
					this.obsService.voidObs(obs, "delete clinical service +" + obsClinicalService.getDisplayString());
				}
				clinicalServices.remove(obsClinicalService);
			}
		}
		
		if (!clinicalServices.isEmpty()) {
			throw new POCBusinessException("Some Clinical services was not deleted: "
			        + StringUtils.join(clinicalServices, "|"));
		}
	}
	
	private List<Concept> getClinicalServices(final ClinicalServiceKeys clinicalServiceKey) {
		
		final List<String> clinicalServiceUuids = MappedClinicalServices.getClinicalServices(clinicalServiceKey);
		
		final List<Concept> clinicalServices = new ArrayList<Concept>();
		
		for (final String clinicalServiceUuid : clinicalServiceUuids) {
			
			clinicalServices.add(this.conceptService.getConceptByUuid(clinicalServiceUuid));
		}
		return clinicalServices;
	}
	
	@Override
	public void setObsService(final ObsService obsService) {
		this.obsService = obsService;
	}
	
	@Override
	public void setConceptService(final ConceptService conceptService) {
		this.conceptService = conceptService;
	}
	
	@Override
	public void setEncounterService(final EncounterService encounterService) {
		this.encounterService = encounterService;
	}
}