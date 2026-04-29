/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.adapter;

import com.mycompany.entapp.snowman.infrastructure.messaging.dto.ClientDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

@RunWith(MockitoJUnitRunner.class)
public class InvoiceSystemAdapterUTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private InvoiceSystemAdapter classUnderTest = new InvoiceSystemAdapter();

    @Test
    public void sendProjectInfo_sendsMessageViaJmsTemplate() {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setClientId(42);
        clientDTO.setClientName("ACME");

        classUnderTest.sendProjectInfo(clientDTO);

        Mockito.verify(jmsTemplate, Mockito.times(1)).send(Matchers.any(MessageCreator.class));
    }
}
