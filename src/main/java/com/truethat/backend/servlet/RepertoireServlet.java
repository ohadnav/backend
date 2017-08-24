package com.truethat.backend.servlet;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.StructuredQuery;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.truethat.backend.common.Util;
import com.truethat.backend.model.Reactable;
import com.truethat.backend.model.User;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.truethat.backend.servlet.TheaterServlet.isValidUser;
import static java.util.stream.Collectors.toList;

/**
 * Proudly created by ohad on 03/07/2017.
 *
 * @android <a>https://github.com/true-that/android/blob/master/app/src/main/java/com/truethat/android/common/network/RepertoireAPI.java</a>
 */
@WebServlet(value = "/repertoire", name = "Repertoire")
public class RepertoireServlet extends BaseServlet {
  @VisibleForTesting static final int FETCH_LIMIT = 10;

  /**
   * Getting the user's repertoire, i.e. the {@link Reactable}s he had created.
   *
   * @param req with {@link User} in its body.
   */
  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    User user = Util.GSON.fromJson(req.getReader(), User.class);
    if (user == null) throw new IOException("Missing user");
    StringBuilder errorBuilder = new StringBuilder();
    if (!isValidUser(datastore, userKeyFactory, user, errorBuilder)) {
      throw new IOException("Invalid user: " + errorBuilder);
    }
    Query<Entity> query = Query.newEntityQueryBuilder().setKind(Reactable.DATASTORE_KIND)
        .setFilter(StructuredQuery.PropertyFilter.eq(Reactable.DATASTORE_DIRECTOR_ID, user.getId()))
        .setLimit(FETCH_LIMIT)
        .build();
    List<Reactable> reactables = Lists.newArrayList(datastore.run(query))
        .stream()
        .map(Reactable::fromEntity)
        .collect(toList());
    reactables.sort(Comparator.comparing(Reactable::getCreated).reversed());
    reactables = reactables.subList(0, Math.min(FETCH_LIMIT, reactables.size()));
    enricher.enrichReactables(reactables, user);
    resp.getWriter().print(Util.GSON.toJson(reactables));
  }
}
