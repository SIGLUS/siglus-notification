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

package org.openlmis.notification.web.subscription;

import static org.openlmis.notification.i18n.MessageKeys.ERROR_INVALID_TAG_IN_SUBSCRIPTION;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_USER_CONTACT_DETAILS_NOT_FOUND;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.openlmis.notification.domain.DigestConfiguration;
import org.openlmis.notification.domain.DigestSubscription;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.DigestConfigurationRepository;
import org.openlmis.notification.repository.DigestSubscriptionRepository;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.openlmis.notification.service.PermissionService;
import org.openlmis.notification.web.NotFoundException;
import org.openlmis.notification.web.ValidationException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DigestSubscriptionController {

  private static final XLogger XLOGGER = XLoggerFactory
      .getXLogger(DigestSubscriptionController.class);

  private static final String USER_ENDPOINT_URL = "/users/{id}/subscriptions";

  @Autowired
  private UserContactDetailsRepository userContactDetailsRepository;

  @Autowired
  private DigestSubscriptionRepository digestSubscriptionRepository;

  @Autowired
  private DigestConfigurationRepository digestConfigurationRepository;

  @Autowired
  private PermissionService permissionService;

  /**
   * Gets users subscriptions.
   *
   * @param userId user's UUID.
   * @return a list of current users subscriptions.
   */
  @GetMapping(USER_ENDPOINT_URL)
  public List<DigestSubscriptionDto> getUserSubscriptions(@PathVariable("id") UUID userId) {
    XLOGGER.entry(userId);
    Profiler profiler = new Profiler("GET_USER_SUBSCRIPTIONS");
    profiler.setLogger(XLOGGER);

    profiler.start("CHECK_PERMISSION");
    permissionService.canManageUserSubscriptions(userId);

    profiler.start("CHECK_IF_USER_CONTACT_DETAILS");
    boolean exists = userContactDetailsRepository.exists(userId);

    if (!exists) {
      NotFoundException exception = new NotFoundException(ERROR_USER_CONTACT_DETAILS_NOT_FOUND);

      profiler.stop().log();
      XLOGGER.throwing(exception);

      throw exception;
    }

    profiler.start("RETRIEVE_USER_SUBSCRIPTIONS");
    List<DigestSubscription> subscriptions = digestSubscriptionRepository
        .getUserSubscriptions(userId);

    profiler.start("CONVERT_TO_DTO");
    List<DigestSubscriptionDto> subscriptionDtos = subscriptions
        .stream()
        .map(DigestSubscriptionDto::newInstance)
        .collect(Collectors.toList());

    profiler.stop().log();
    XLOGGER.exit(subscriptionDtos);

    return subscriptionDtos;
  }

  /**
   * Creates users subscriptions. Old subscriptions would be removed and to avoid that they should
   * be inside the request body.
   *
   * @param userId user's UUID.
   * @param subscriptions a list of users subscriptions.
   * @return a list of current users subscriptions.
   */
  @PostMapping(USER_ENDPOINT_URL)
  public List<DigestSubscriptionDto> createUserSubscriptions(@PathVariable("id") UUID userId,
      @RequestBody List<DigestSubscriptionDto> subscriptions) {
    XLOGGER.entry(userId, subscriptions);
    Profiler profiler = new Profiler("CREATE_USER_SUBSCRIPTIONS");
    profiler.setLogger(XLOGGER);

    profiler.start("CHECK_PERMISSION");
    permissionService.canManageUserSubscriptions(userId);

    profiler.start("GET_USER_CONTACT_DETAILS");
    UserContactDetails contactDetails = userContactDetailsRepository.findOne(userId);

    if (Objects.isNull(contactDetails)) {
      NotFoundException exception = new NotFoundException(ERROR_USER_CONTACT_DETAILS_NOT_FOUND);

      profiler.stop().log();
      XLOGGER.throwing(exception);

      throw exception;
    }

    profiler.start("GET_DIGEST_CONFIGURATION_TAGS_FROM_REQUEST");
    Set<String> tags = subscriptions
        .stream()
        .map(DigestSubscriptionDto::getDigestConfigurationTag)
        .collect(Collectors.toSet());

    profiler.start("GET_DIGEST_CONFIGURATIONS");
    Map<String, DigestConfiguration> configurations = digestConfigurationRepository
        .findByTagIn(tags)
        .stream()
        .collect(Collectors.toMap(DigestConfiguration::getTag, Function.identity()));

    profiler.start("CONVERT_TO_DOMAIN");
    List<DigestSubscription> digestSubscriptions = Lists.newArrayList();
    for (int i = 0, size = subscriptions.size(); i < size; i++) {
      DigestSubscriptionDto subscriptionDto = subscriptions.get(i);
      DigestConfiguration digestConfiguration = configurations
          .get(subscriptionDto.getDigestConfigurationTag());

      if (null == digestConfiguration) {
        ValidationException exception = new ValidationException(
            ERROR_INVALID_TAG_IN_SUBSCRIPTION, subscriptionDto.getDigestConfigurationTag());

        profiler.stop().log();
        XLOGGER.throwing(exception);

        throw exception;
      }

      digestSubscriptions.add(new DigestSubscription(
          contactDetails, digestConfiguration, subscriptionDto.getTime()));
    }

    profiler.start("DELETE_OLD_USER_SUBSCRIPTIONS");
    digestSubscriptionRepository.deleteUserSubscriptions(userId);

    profiler.start("SAVE_USER_SUBSCRIPTIONS");
    digestSubscriptions = digestSubscriptionRepository.save(digestSubscriptions);

    profiler.start("CONVERT_TO_DTO");
    List<DigestSubscriptionDto> subscriptionDtos = digestSubscriptions
        .stream()
        .map(DigestSubscriptionDto::newInstance)
        .collect(Collectors.toList());

    profiler.stop().log();
    XLOGGER.exit(subscriptionDtos);

    return subscriptionDtos;
  }

}
