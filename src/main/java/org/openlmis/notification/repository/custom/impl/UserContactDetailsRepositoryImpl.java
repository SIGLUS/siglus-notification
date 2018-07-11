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

package org.openlmis.notification.repository.custom.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.custom.UserContactDetailsRepositoryCustom;
import org.openlmis.notification.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class UserContactDetailsRepositoryImpl implements UserContactDetailsRepositoryCustom {

  private static final String ID = "referenceDataUserId";
  private static final String EMAIL = "emailDetails.email";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Method returns all matching user contact details. If all parameters are null, returns all user
   * contact details. For email: matches values that equal or contain the searched value. Case
   * insensitive. Other fields: entered string value must equal to searched value.
   *
   * @return Page of user contact details
   */
  public Page<UserContactDetails> search(String email, Set<UUID> ids, Pageable pageable) {

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<UserContactDetails> query = builder.createQuery(UserContactDetails.class);
    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);

    query = prepareQuery(email, ids, query, false, pageable);
    countQuery = prepareQuery(email, ids, countQuery, true, pageable);

    Long count = entityManager.createQuery(countQuery).getSingleResult();

    List<UserContactDetails> result = entityManager.createQuery(query)
        .setMaxResults(pageable.getPageSize())
        .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
        .getResultList();

    return Pagination.getPage(result, pageable, count);
  }

  private <T> CriteriaQuery<T> prepareQuery(String email, Set<UUID> ids, CriteriaQuery<T> query,
      boolean count, Pageable pageable) {

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    Root<UserContactDetails> root = query.from(UserContactDetails.class);
    Predicate predicate = builder.conjunction();

    if (count) {
      CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) query;
      query = (CriteriaQuery<T>) countQuery.select(builder.count(root));
    }

    predicate = addLikeFilter(predicate, builder, getField(root, EMAIL), email);
    predicate = addInFilter(predicate, builder, getField(root, ID), ids);

    query.where(predicate);

    if (!count && pageable.getSort() != null) {
      query = addSortProperties(query, root, pageable);
    }

    return query;
  }

  private <T> CriteriaQuery<T> addSortProperties(CriteriaQuery<T> query,
      Root<UserContactDetails> root, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    List<Order> orders = new ArrayList<>();
    Iterator<Sort.Order> iterator = pageable.getSort().iterator();

    Sort.Order order;
    while (iterator.hasNext()) {
      order = iterator.next();
      String property = order.getProperty();
      Expression field = getField(root, property);

      if (order.isAscending()) {
        orders.add(builder.asc(field));
      } else {
        orders.add(builder.desc(field));
      }
    }

    return query.orderBy(orders);
  }

  private Predicate addLikeFilter(Predicate predicate, CriteriaBuilder builder,
      Expression<String> field, String filterValue) {
    return filterValue != null
        ? builder.and(predicate, builder.like(
            builder.upper(field), "%" + filterValue.toUpperCase() + "%"))
        : predicate;
  }

  private Predicate addInFilter(Predicate predicate, CriteriaBuilder builder,
      Expression<?> field, Collection values) {
    return null == values || values.isEmpty()
        ? predicate
        : builder.and(predicate, field.in(values));
  }

  private <Y> Expression<Y> getField(Root<UserContactDetails> root, String field) {
    String[] fields = field.split("\\.");

    if (fields.length < 2) {
      return root.get(field);
    }

    Path<Y> path = root.get(fields[0]);
    for (int i = 1, length = fields.length; i < length; ++i) {
      path = path.get(fields[i]);
    }

    return path;
  }

}
