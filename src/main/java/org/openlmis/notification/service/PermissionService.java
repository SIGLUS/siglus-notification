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


import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.util.UUID;
import org.openlmis.notification.service.referencedata.RightDto;
import org.openlmis.notification.service.referencedata.UserDto;
import org.openlmis.notification.service.referencedata.UserReferenceDataService;
import org.openlmis.notification.util.AuthenticationHelper;
import org.openlmis.notification.web.MissingPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("PMD.TooManyMethods")
public class PermissionService {
  static final String ORDERS_TRANSFER = "ORDERS_TRANSFER";
  public static final String PODS_MANAGE = "PODS_MANAGE";
  public static final String PODS_VIEW = "PODS_VIEW";
  public static final String ORDERS_VIEW = "ORDERS_VIEW";
  public static final String ORDERS_EDIT = "ORDERS_EDIT";
  public static final String SHIPMENTS_VIEW = "SHIPMENTS_VIEW";
  public static final String SHIPMENTS_EDIT = "SHIPMENTS_EDIT";
  static final String SYSTEM_SETTINGS_MANAGE = "SYSTEM_SETTINGS_MANAGE";

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Value("${auth.server.clientId}")
  private String serviceTokenClientId;

  @Value("${auth.server.clientId.apiKey.prefix}")
  private String apiKeyPrefix;

  private void checkPermission(String rightName, UUID facility, UUID program) {
    checkPermission(rightName, facility, program, null, true, false);
  }

  private void checkPermission(String rightName, UUID warehouse) {
    checkPermission(rightName, null, null, warehouse, true, false);
  }

  private void checkPermission(String rightName, UUID facility, UUID program, UUID warehouse,
                               boolean allowUserTokens, boolean allowApiKey) {
    if (hasPermission(rightName, facility, program, warehouse, allowUserTokens, allowApiKey)) {
      return;
    }

    throw new MissingPermissionException(rightName);
  }

  private boolean hasPermission(String rightName, UUID facility, UUID program) {
    return hasPermission(rightName, facility, program, null, true, false);
  }

  private boolean hasPermission(String rightName, UUID warehouse) {
    return hasPermission(rightName, null, null, warehouse, true, false);
  }

  private boolean hasPermission(String rightName, UUID facility, UUID program, UUID warehouse,
                                boolean allowUserTokens, boolean allowApiKey) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
        .getContext()
        .getAuthentication();

    return authentication.isClientOnly()
        ? checkServiceToken(allowApiKey, authentication)
        : checkUserToken(rightName, facility, program, warehouse, allowUserTokens);
  }

  private boolean checkUserToken(String rightName, UUID facility, UUID program, UUID warehouse,
                                 boolean allowUserTokens) {
    if (!allowUserTokens) {
      return false;
    }

    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);
    ResultDto<Boolean> result =  userReferenceDataService.hasRight(
        user.getId(), right.getId(), program, facility, warehouse
    );

    return null != result && isTrue(result.getResult());
  }

  private boolean checkServiceToken(boolean allowApiKey,
                                    OAuth2Authentication authentication) {
    String clientId = authentication.getOAuth2Request().getClientId();

    if (serviceTokenClientId.equals(clientId)) {
      return true;
    }

    if (startsWith(clientId, apiKeyPrefix)) {
      return allowApiKey;
    }

    return false;
  }

}
