package org.kostenko.example.wildfly.hibernatesearch;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Test;
import javax.persistence.Persistence;
import junit.framework.Assert;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.junit.BeforeClass;

/**
 * @author kostenko
 */
public class JpaHibernateSearchTest {

    private static EntityManager entityManager;
    private static FullTextEntityManager fullTextEntityManager;
    private static QueryBuilder queryBuilder;

    @BeforeClass
    public static void init() {
        entityManager = Persistence.createEntityManagerFactory("myDSTest").createEntityManager();
        fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(BlogEntity.class).get();
    }

    @Test
    public void shouldPersistBlogEntity() {
        for (int i = 0; i < 1000; i++) {
            BlogEntity blogEntity = new BlogEntity();
            blogEntity.setTitle("Title" + i);
            blogEntity.setBody("BodyBody Body" + i + " look at my horse my horse is a amazing " + i);
            entityManager.getTransaction().begin();
            entityManager.persist(blogEntity);
            entityManager.getTransaction().commit();
        }
        Assert.assertEquals(1000, entityManager.createQuery("SELECT COUNT(b) FROM BlogEntity b", Number.class).getSingleResult().intValue());
    }

    /**
     * Keyword Queries - searching for a specific word.
     */
    @Test
    public void shouldSearchByKeywordQuery() throws Exception {

        Query query = queryBuilder.keyword().onFields("title", "body").matching("Body999").createQuery();

        javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, BlogEntity.class); // wrap Lucene query in a javax.persistence.Query
        List<BlogEntity> result = persistenceQuery.getResultList();// execute search
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals("Title999", result.get(0).getTitle());
    }

    /**
     * Fuzzy Queries - we can define a limit of “fuzziness”
     */
    @Test
    public void shouldSearchByFuzzyQuery() throws Exception {

        Query query = queryBuilder.keyword().fuzzy().withEditDistanceUpTo(2).withPrefixLength(0).onField("title").matching("TAtle999").createQuery();

        javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, BlogEntity.class);
        List<BlogEntity> result = persistenceQuery.getResultList();// execute search
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals("Title999", result.get(0).getTitle());
    }

    /**
     * Wildcard Queries - queries for which a part of a word is unknown ('?' - single character, '*' - character sequence)
     */
    @Test
    public void shouldSearchByWildcardQuery() throws Exception {

        Query query = queryBuilder.keyword().wildcard().onField("title").matching("?itle*").createQuery();

        javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, BlogEntity.class);
        List<BlogEntity> result = persistenceQuery.getResultList();// execute search
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(1000, result.size());
    }

    /**
     * Phrase Queries - search for exact or for approximate sentences
     */
    @Test
    public void shouldSearchByPhraseQuery() throws Exception {

        Query query = queryBuilder.phrase().withSlop(10).onField("body").sentence("look amazing horse 999").createQuery();

        javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, BlogEntity.class);
        List<BlogEntity> result = persistenceQuery.getResultList();
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals("Title999", result.get(0).getTitle());
    }
    

}
