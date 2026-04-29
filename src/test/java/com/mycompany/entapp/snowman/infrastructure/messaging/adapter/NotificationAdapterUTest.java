/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.adapter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;

@RunWith(MockitoJUnitRunner.class)
public class NotificationAdapterUTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private NotificationAdapter classUnderTest = new NotificationAdapter();

    @Test
    public void broadcastUpdates_callsConvertAndSendOnJmsTemplate() {
        Object payload = new Object();

        classUnderTest.broadcastUpdates(payload);

        Mockito.verify(jmsTemplate, Mockito.times(1)).convertAndSend(payload);
    }
}
