package com.axelor.apps.dailyts.db.repo;

import com.axelor.apps.hr.db.repo.TimesheetHRRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetLineComputeNameService;
import com.axelor.apps.hr.service.timesheet.TimesheetPeriodComputationService;
import jakarta.inject.Inject;

public class TimesheetDailytsRepository extends TimesheetHRRepository {

  @Inject
  public TimesheetDailytsRepository(
      TimesheetLineComputeNameService timesheetLineComputeNameService,
      TimesheetPeriodComputationService timesheetPeriodComputationService) {
    super(timesheetLineComputeNameService, timesheetPeriodComputationService);
  }
}
