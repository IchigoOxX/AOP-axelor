package com.axelor.apps.dailyts.db.repo;

import com.axelor.apps.hr.db.repo.TimesheetLineHRRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetLineComputeNameService;
import jakarta.inject.Inject;

public class TimesheetLineDailytsRepository extends TimesheetLineHRRepository {

  @Inject
  public TimesheetLineDailytsRepository(
      TimesheetLineComputeNameService timesheetLineComputeNameService) {
    super(timesheetLineComputeNameService);
  }
}
