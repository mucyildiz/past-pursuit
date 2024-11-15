package org.pastpursuit;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class ResultRepository {
  private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("gameResult");
  public void save(GameResult gameResult) {
    EntityManager entityManager = emf.createEntityManager();
    entityManager.getTransaction().begin();
    entityManager.persist(gameResult);
    entityManager.getTransaction().commit();
    entityManager.close();
  }
}
