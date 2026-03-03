/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.adapter;

import com.mycompany.entapp.snowman.infrastructure.messaging.InvoiceSystemPort;
import com.mycompany.entapp.snowman.infrastructure.messaging.dto.ClientDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.ObjectMessage;

@Component
public class InvoiceSystemAdapter implements InvoiceSystemPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceSystemAdapter.class);

    @Autowired
    @Qualifier("invoiceJmsTemplate")
    private JmsTemplate jmsTemplate;

    @Override
    public void sendProjectInfo(final ClientDTO clientDTO) {
        LOGGER.info("Sending client info to Invoice System: {}", clientDTO);
        jmsTemplate.send(session -> {
            ObjectMessage objectMessage = session.createObjectMessage(clientDTO);
            // EIP - correlate at the other end
            objectMessage.setJMSCorrelationID("ClientID-" + clientDTO.getClientId());
            return objectMessage;
        });
    }

}
