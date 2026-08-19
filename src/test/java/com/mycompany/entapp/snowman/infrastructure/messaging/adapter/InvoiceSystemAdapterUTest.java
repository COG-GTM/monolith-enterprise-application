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

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class InvoiceSystemAdapterUTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Session session;

    @Mock
    private ObjectMessage objectMessage;

    @InjectMocks
    private InvoiceSystemAdapter systemUnderTest = new InvoiceSystemAdapter();

    @Test
    public void testSendProjectInfoShouldSendObjectMessageCorrelatedOnClientId() throws JMSException {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setClientId(7);
        clientDTO.setClientName("Client");
        Mockito.when(session.createObjectMessage(Mockito.any(ClientDTO.class))).thenReturn(objectMessage);

        systemUnderTest.sendProjectInfo(clientDTO);

        ArgumentCaptor<MessageCreator> captor = ArgumentCaptor.forClass(MessageCreator.class);
        Mockito.verify(jmsTemplate).send(captor.capture());

        assertEquals(objectMessage, captor.getValue().createMessage(session));
        Mockito.verify(session).createObjectMessage(clientDTO);
        Mockito.verify(objectMessage).setJMSCorrelationID("ClientID-7");
    }
}
