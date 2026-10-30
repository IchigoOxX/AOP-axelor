package com.gl.apps.pos.service;

import com.gl.apps.pos.service.PosSequenceService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.db.JPA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Inject;

public interface PosSequenceService {
	public String getNextOrderNumber(Long posConfigId);
	public String getNextSessionNumber(Long posConfigId);
	public void resetSequence(Long posConfigId);
	public String getSequenceNumber(String type, Long sequenceId);
}

