package org.kostenko.example.wildfly.hibernatesearch;

import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 *
 * @author kostenko
 */
@Path("/")
@Stateless
public class HibernateSearchDemoEndpoint {

    @PersistenceContext
    EntityManager entityManager;

    @GET
    @Path("/init")
    @Transactional
    public Response init() {
        
       int count = entityManager.createQuery("SELECT COUNT(b) FROM BlogEntity b", Number.class).getSingleResult().intValue();
        
        long time = System.currentTimeMillis();
        for (int i = count; i < count + 1000; i++) {
            BlogEntity blogEntity = new BlogEntity();
            blogEntity.setTitle("Title" + i);
            blogEntity.setBody("Body Body Body" + i + " look at my horse my horse is a amazing " + i);
            entityManager.persist(blogEntity);
        }
        time = System.currentTimeMillis() - time;
        return Response.ok().entity(String.format("1000 records persisted. Current records %s Execution time = %s ms.", count + 1000, time)).build();
    }

    @GET
    @Path("/search")
    public Response search(@QueryParam("q") String q) {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(BlogEntity.class).get();

        long time = System.currentTimeMillis();
        Query query = queryBuilder.keyword().onFields("title", "body").matching(q).createQuery();
        javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, BlogEntity.class);
        List<BlogEntity> result = persistenceQuery.getResultList();
        time = System.currentTimeMillis() - time;

        return Response.ok().entity(
                String.format("Found %s results. [%s] Execution time = %s ms.",
                        result.size(), 
                        result.stream().map(Object::toString).collect(Collectors.joining(",")),
                        time)
        ).build();
    }

}
