/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.application.schedule.service.impl;

import com.mycompany.entapp.snowman.application.schedule.ReportingData;
import com.mycompany.entapp.snowman.domain.service.ApplicationInfoService;
import com.mycompany.entapp.snowman.domain.service.ClientService;
import com.mycompany.entapp.snowman.domain.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportingServiceImplUTest {

    @Mock
    private ApplicationInfoService applicationInfoService;

    @Mock
    private ClientService clientService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReportingServiceImpl systemUnderTest = new ReportingServiceImpl();

    @Test
    public void testRetrieveReportingDataReturnsCurrentImplementationResult() {
        // retrieveReportingData is currently an unimplemented stub returning null.
        // This test pins the current behaviour so future implementations update it deliberately.
        ReportingData reportingData = systemUnderTest.retrieveReportingData();

        assertNull(reportingData);
    }
}
