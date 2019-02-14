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
    return ((Notification) message.getPayload())
        .getMessages()
        .stream()
        .filter(item -> !item.getSend())
        .map(item -> MessageBuilder
            .withPayload(item)
            .copyHeaders(message.getHeaders())
            .setHeader(CHANNEL_HEADER, item.getChannel())
            .build())
        .collect(Collectors.toSet());
  }

}
