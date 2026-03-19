/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.adapter;

import com.mycompany.entapp.snowman.infrastructure.messaging.NotificationPort;
import com.mycompany.entapp.snowman.infrastructure.messaging.dto.NotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationAdapter implements NotificationPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationAdapter.class);

    @Autowired
    @Qualifier("notificationJmsTemplate")
    private JmsTemplate jmsTemplate;

    @Override
    public void broadcastUpdates(NotificationDTO notification) {
        if (notification == null) {
            LOGGER.warn("Attempted to broadcast a null notification, ignoring");
            return;
        }
        LOGGER.info("Sending notification [entityType={}, entityId={}, action={}]",
            notification.getEntityType(), notification.getEntityId(), notification.getAction());
        jmsTemplate.convertAndSend(notification);
    }
}
