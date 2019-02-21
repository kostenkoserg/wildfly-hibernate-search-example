package org.kostenko.example.wildfly.hibernatesearch;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Test;

import javax.persistence.Persistence;
import javax.persistence.Query;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.junit.BeforeClass;

/**
 * @author kostenko
 */
public class JPATest {

    private static EntityManager entityManager;

    @BeforeClass
    public static void init() {
        entityManager = Persistence.createEntityManagerFactory("myDSTest").createEntityManager();
    }

    @Test
    public void shouldPersistBlogEntity() {
        for (int i = 0; i < 1000; i++) {
            BlogEntity blogEntity = new BlogEntity();
            blogEntity.setTitle("Title" + i);
            blogEntity.setBody("BodyBody Body" + i + " dadsdsds asasasasxa");
            entityManager.getTransaction().begin();
            entityManager.persist(blogEntity);
            entityManager.getTransaction().commit();
        }
    }

    @Test
    public void shouldSearchByIndex() throws Exception {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        
        // create native Lucene query
        String[] fields = new String[]{"title", "body"};
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
        org.apache.lucene.search.Query query = parser.parse("Body999");

        // wrap Lucene query in a javax.persistence.Query
        Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, BlogEntity.class);

        // execute search
        List<BlogEntity> result = persistenceQuery.getResultList();
        
        if (!result.isEmpty()) {
             System.out.println("Title: " + result.get(0).getTitle());
        }
        System.out.println("--- " + result.size());
    }
}
