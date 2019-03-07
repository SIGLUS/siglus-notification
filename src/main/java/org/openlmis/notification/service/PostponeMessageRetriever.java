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

import static org.openlmis.notification.service.NotificationToSendRetriever.RECIPIENT_HEADER;
import static org.openlmis.notification.service.NotificationTransformer.CHANNEL_HEADER;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import org.openlmis.notification.domain.PostponeMessage;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.jpa.core.JpaExecutor;
import org.springframework.integration.jpa.support.JpaParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

class PostponeMessageRetriever implements MessageSource<List<PostponeMessage>> {

  static final String CONFIGURATION_ID_HEADER = "configurationId";

  private JpaExecutor jpaExecutor;

  private NotificationChannel channel;
  private UUID configurationId;
  private UUID userId;

  PostponeMessageRetriever(EntityManager entityManager,
      NotificationChannel channel, UUID configurationId, UUID userId) {
    this((JpaExecutor) null, channel, configurationId, userId);
    prepareJpaExecutor(entityManager);
  }

  @VisibleForTesting
  PostponeMessageRetriever(JpaExecutor jpaExecutor, NotificationChannel channel,
      UUID configurationId, UUID userId) {
    this.jpaExecutor = jpaExecutor;
    this.channel = channel;
    this.configurationId = configurationId;
    this.userId = userId;
  }

  @Override
  public Message<List<PostponeMessage>> receive() {
    Object polled = jpaExecutor.poll();

    if (null == polled) {
      return null;
    }

    List<PostponeMessage> collection = Lists.newArrayList();

    if (polled instanceof PostponeMessage) {
      collection.add((PostponeMessage) polled);
    } else if (polled instanceof Collection) {
      for (Object o : (Collection) polled) {
        collection.add((PostponeMessage) o);
      }
    }

    if (collection.isEmpty()) {
      return null;
    }

    return MessageBuilder
        .withPayload(collection)
        .setHeader(RECIPIENT_HEADER, userId)
        .setHeader(CONFIGURATION_ID_HEADER, configurationId)
        .setHeader(CHANNEL_HEADER, channel)
        .build();
  }

  private void prepareJpaExecutor(EntityManager entityManager) {
    jpaExecutor = new JpaExecutor(entityManager);

    jpaExecutor.setEntityClass(PostponeMessage.class);
    jpaExecutor.setNamedQuery(PostponeMessage.GET_POSTPONE_MESSAGES_NAMED_QUERY);
    jpaExecutor.setJpaParameters(Lists.newArrayList(
        createParameter(CONFIGURATION_ID_HEADER, configurationId),
        createParameter("channel", channel),
        createParameter("userId", userId)
    ));

    jpaExecutor.setExpectSingleResult(false);

    jpaExecutor.setUsePayloadAsParameterSource(true);

    jpaExecutor.setDeleteAfterPoll(true);
    jpaExecutor.setFlush(true);
    jpaExecutor.setClearOnFlush(true);
  }

  private JpaParameter createParameter(String name, Object value) {
    return new JpaParameter(name, value, String.format("'%s'", value));
  }

}
