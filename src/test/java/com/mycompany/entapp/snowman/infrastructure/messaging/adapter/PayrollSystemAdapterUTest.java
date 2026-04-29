/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.adapter;

import com.mycompany.entapp.snowman.infrastructure.messaging.dto.EmployeeDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

@RunWith(MockitoJUnitRunner.class)
public class PayrollSystemAdapterUTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private PayrollSystemAdapter classUnderTest = new PayrollSystemAdapter();

    @Test
    public void sendEmployeeInfo_sendsViaJmsTemplate() {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(123);
        employeeDTO.setFirstName("Alice");
        employeeDTO.setSurname("Smith");
        employeeDTO.setRole("Developer");

        classUnderTest.sendEmployeeInfo(employeeDTO);

        Mockito.verify(jmsTemplate, Mockito.times(1))
            .convertAndSend(Matchers.eq(employeeDTO), Matchers.any(MessagePostProcessor.class));
    }
}
