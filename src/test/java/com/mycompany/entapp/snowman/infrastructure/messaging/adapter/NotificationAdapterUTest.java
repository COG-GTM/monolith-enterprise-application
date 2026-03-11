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
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class NotificationAdapterUTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private NotificationAdapter classInTest;

    @Test
    public void testBroadcastUpdates() {
        String testUpdate = "test-update";

        classInTest.broadcastUpdates(testUpdate);

        verify(jmsTemplate, times(1)).convertAndSend(testUpdate);
    }
}
