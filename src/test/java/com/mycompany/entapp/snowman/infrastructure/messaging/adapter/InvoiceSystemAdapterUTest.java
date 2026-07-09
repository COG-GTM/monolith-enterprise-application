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
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.ObjectMessage;
import javax.jms.Session;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class InvoiceSystemAdapterUTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private InvoiceSystemAdapter systemUnderTest = new InvoiceSystemAdapter();

    @Test
    public void testSendProjectInfoShouldSendMessage() throws Exception {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setClientId(9);
        clientDTO.setClientName("Acme");

        systemUnderTest.sendProjectInfo(clientDTO);

        ArgumentCaptor<MessageCreator> captor = ArgumentCaptor.forClass(MessageCreator.class);
        Mockito.verify(jmsTemplate, Mockito.times(1)).send(captor.capture());

        Session session = Mockito.mock(Session.class);
        ObjectMessage objectMessage = Mockito.mock(ObjectMessage.class);
        Mockito.when(session.createObjectMessage(clientDTO)).thenReturn(objectMessage);

        captor.getValue().createMessage(session);

        Mockito.verify(session, Mockito.times(1)).createObjectMessage(clientDTO);
        Mockito.verify(objectMessage, Mockito.times(1)).setJMSCorrelationID("ClientID-9");
    }
}
