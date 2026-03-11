/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.adapter;

import com.mycompany.entapp.snowman.infrastructure.messaging.dto.EmployeeDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import javax.jms.DeliveryMode;
import javax.jms.Message;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PayrollSystemAdapterUTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private PayrollSystemAdapter classInTest;

    @Test
    public void testSendEmployeeInfo() throws Exception {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(7);

        classInTest.sendEmployeeInfo(employeeDTO);

        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate, times(1)).convertAndSend(eq(employeeDTO), postProcessorCaptor.capture());

        MessagePostProcessor postProcessor = postProcessorCaptor.getValue();

        Message message = mock(Message.class);
        postProcessor.postProcessMessage(message);

        verify(message).setJMSCorrelationID("EmployeeId-7");
        verify(message).setBooleanProperty("pristine", true);
        verify(message).setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
        verify(message).setJMSMessageID("123-0000-7");
        verify(message).setJMSPriority(1);
        verify(message).setJMSExpiration(5000L);
    }
}
