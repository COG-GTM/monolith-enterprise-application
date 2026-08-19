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
import javax.jms.JMSException;
import javax.jms.Message;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PayrollSystemAdapterUTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Message message;

    @InjectMocks
    private PayrollSystemAdapter systemUnderTest = new PayrollSystemAdapter();

    @Test
    public void testSendEmployeeInfoShouldPostProcessMessage() throws JMSException {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(9);

        systemUnderTest.sendEmployeeInfo(employeeDTO);

        ArgumentCaptor<MessagePostProcessor> captor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        Mockito.verify(jmsTemplate).convertAndSend(Mockito.eq(employeeDTO), captor.capture());

        assertEquals(message, captor.getValue().postProcessMessage(message));
        Mockito.verify(message).setJMSCorrelationID("EmployeeId-9");
        Mockito.verify(message).setBooleanProperty("pristine", true);
        Mockito.verify(message).setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
        Mockito.verify(message).setJMSMessageID("123-0000-9");
        Mockito.verify(message).setJMSPriority(1);
        Mockito.verify(message).setJMSExpiration(5000L);
    }
}
