/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.notification.service;

import static org.openlmis.notification.service.DigestFilter.SEND_NOW_PREPARE_CHANNEL;
import static org.openlmis.notification.service.NotificationToSendRetriever.CHANNELS_HEADER;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.openlmis.notification.domain.Notification;
import org.openlmis.notification.domain.NotificationMessage;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

@MessageEndpoint
public class NotificationSplitter {

  static final String READY_TO_SEND_CHANNEL = "notificationToSend.sendNow.readyToSend";

  static final String CHANNEL_HEADER = "channel";

  /**
   * Split single notification into several messages. Skips messages that have been sent.
   */
  @Splitter(inputChannel = SEND_NOW_PREPARE_CHANNEL, outputChannel = READY_TO_SEND_CHANNEL)
  public Set<Message<NotificationMessage>> extractNotificationMessages(Message<?> message) {
    System.out.println("SPLITTER");

    // we set a correct set collection at the beginning of the flow
    @SuppressWarnings("unchecked")
    Set<NotificationChannel> channels = Collections.checkedSet(
        message.getHeaders().get(CHANNELS_HEADER, Set.class),
        NotificationChannel.class
    );

    Notification notification = (Notification) message.getPayload();

    return notification
        .getMessages()
        .stream()
        .filter(item -> channels.contains(item.getChannel()))
        .map(item -> MessageBuilder
            .withPayload(item)
            .copyHeaders(message.getHeaders())
            .setHeader(CHANNEL_HEADER, item.getChannel())
            .removeHeader(CHANNELS_HEADER)
            .build())
        .collect(Collectors.toSet());
  }

}
