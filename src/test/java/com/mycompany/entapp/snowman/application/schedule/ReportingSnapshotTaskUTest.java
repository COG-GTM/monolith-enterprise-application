/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.application.schedule;

import com.mycompany.entapp.snowman.application.schedule.service.ReportingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReportingSnapshotTaskUTest {

    @Mock
    private ReportingService reportingService;

    @InjectMocks
    private ReportingSnapshotTask classUnderTest;

    @Test
    public void testExecuteTask() {
        ReportingData reportingData = new ReportingData();
        Mockito.when(reportingService.retrieveReportingData()).thenReturn(reportingData);

        classUnderTest.executeTask();

        Mockito.verify(reportingService, Mockito.times(1)).retrieveReportingData();
    }
}
