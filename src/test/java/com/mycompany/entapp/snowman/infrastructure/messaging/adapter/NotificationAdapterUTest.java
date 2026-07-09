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
    private NotificationAdapter systemUnderTest = new NotificationAdapter();

    @Test
    public void testBroadcastUpdatesShouldConvertAndSend() {
        Object payload = new Object();

        systemUnderTest.broadcastUpdates(payload);

        Mockito.verify(jmsTemplate, Mockito.times(1)).convertAndSend(payload);
    }
}
