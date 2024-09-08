package org.c4rth.jpah2.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyRepository extends JpaRepository<DummyEntity, Long>, CrudRepository<DummyEntity, Long> {
}