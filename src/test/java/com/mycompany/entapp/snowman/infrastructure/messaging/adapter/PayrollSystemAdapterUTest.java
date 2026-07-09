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
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import javax.jms.DeliveryMode;
import javax.jms.Message;

@RunWith(MockitoJUnitRunner.class)
public class PayrollSystemAdapterUTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private PayrollSystemAdapter systemUnderTest = new PayrollSystemAdapter();

    @Test
    public void testSendEmployeeInfoShouldConvertAndSendWithPostProcessor() throws Exception {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(11);

        systemUnderTest.sendEmployeeInfo(employeeDTO);

        ArgumentCaptor<MessagePostProcessor> captor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        Mockito.verify(jmsTemplate, Mockito.times(1)).convertAndSend(Mockito.eq(employeeDTO), captor.capture());

        Message message = Mockito.mock(Message.class);
        Message processed = captor.getValue().postProcessMessage(message);

        Mockito.verify(message, Mockito.times(1)).setJMSCorrelationID("EmployeeId-11");
        Mockito.verify(message, Mockito.times(1)).setBooleanProperty("pristine", true);
        Mockito.verify(message, Mockito.times(1)).setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
        Mockito.verify(message, Mockito.times(1)).setJMSMessageID("123-0000-11");
        org.junit.Assert.assertSame(message, processed);
    }
}
