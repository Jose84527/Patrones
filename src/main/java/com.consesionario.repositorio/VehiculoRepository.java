/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.consesionario.fabrica;

/**
 *
 * @author josej
 */

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class VehiculoRepository {
    private final EntityManager em;

    public VehiculoRepository(EntityManager em) {
        this.em = em;
    }

    public <T> T save(T entity) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}

