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

import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class ReportingServiceImplUTest {

    @Mock
    private ApplicationInfoService applicationInfoService;

    @Mock
    private ClientService clientService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReportingServiceImpl classUnderTest;

    @Test
    public void testRetrieveReportingData() {
        // TODO: Implementation is incomplete - retrieveReportingData() currently returns null.
        // Update this test once the implementation is completed.
        ReportingData result = classUnderTest.retrieveReportingData();

        assertNull(result);
    }
}
