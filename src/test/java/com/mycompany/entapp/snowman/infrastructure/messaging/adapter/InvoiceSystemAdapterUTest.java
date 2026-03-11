/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.adapter;

import com.mycompany.entapp.snowman.infrastructure.messaging.dto.ClientDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.ObjectMessage;
import javax.jms.Session;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InvoiceSystemAdapterUTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private InvoiceSystemAdapter classInTest;

    @Test
    public void testSendProjectInfo() throws Exception {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setClientId(42);
        clientDTO.setClientName("Test Client");

        classInTest.sendProjectInfo(clientDTO);

        ArgumentCaptor<MessageCreator> messageCreatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate, times(1)).send(messageCreatorCaptor.capture());

        MessageCreator messageCreator = messageCreatorCaptor.getValue();

        Session session = mock(Session.class);
        ObjectMessage objectMessage = mock(ObjectMessage.class);
        when(session.createObjectMessage(clientDTO)).thenReturn(objectMessage);

        messageCreator.createMessage(session);

        verify(session).createObjectMessage(clientDTO);
        verify(objectMessage).setJMSCorrelationID("ClientID-42");
    }
}
