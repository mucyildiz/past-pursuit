package org.pastpursuit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class UserRepository {
  private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("user");

  public User save(User user) {
    EntityManager entityManager = emf.createEntityManager();
    entityManager.getTransaction().begin();
    entityManager.persist(user);
    entityManager.getTransaction().commit();
    entityManager.close();
    return user;
  }

  public void update(User user) {
    EntityManager entityManager = emf.createEntityManager();
    entityManager.getTransaction().begin();
    entityManager.merge(user);
    entityManager.getTransaction().commit();
    entityManager.close();
  }
}
