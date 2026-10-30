package com.gl.apps.pos.service.impl;

import com.gl.apps.pos.service.PosSequenceService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.db.JPA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

public class PosSequenceServiceImpl implements PosSequenceService {

  private static final Logger LOG = LoggerFactory.getLogger(PosSequenceServiceImpl.class);

  @Inject private SequenceService sequenceService;

  @Override
  public String getNextOrderNumber(Long posConfigId) {
    LOG.debug("Getting next order number for config: {}", posConfigId);
    com.axelor.studio.db.AppPos config = JPA.find(com.axelor.studio.db.AppPos.class, posConfigId);
    
    if (config == null || null == null) {
      throw new RuntimeException("POS Config or Order Sequence not configured");
    }

    return java.util.UUID.randomUUID().toString().substring(0,8);
  }

  @Override
  public String getNextSessionNumber(Long posConfigId) {
    LOG.debug("Getting next session number for config: {}", posConfigId);
    com.axelor.studio.db.AppPos config = JPA.find(com.axelor.studio.db.AppPos.class, posConfigId);
    
    if (config == null || null == null) {
      throw new RuntimeException("POS Config or Session Sequence not configured");
    }

    return java.util.UUID.randomUUID().toString().substring(0,8);
  }

  @Override
  public void resetSequence(Long posConfigId) {
    LOG.info("Resetting sequences for config: {}", posConfigId);
    // TODO: Implement sequence reset logic
  }

  @Override
  public String getSequenceNumber(String type, Long sequenceId) {
    LOG.debug("Getting sequence number - Type: {}, Sequence ID: {}", type, sequenceId);
    return java.util.UUID.randomUUID().toString().substring(0,8);
  }
}





