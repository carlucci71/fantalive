package com.daniele.fantalive.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.daniele.fantalive.entity.Salva;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SalvaRepository extends CrudRepository<Salva, String> {

	@Query(value = "select * from salva where nome like :name", nativeQuery = true)
	List<Salva> findSimulazioniName(@Param("name") String name);

    @Query(value = "select max(nome) from salva where nome like :name", nativeQuery = true)
    Optional<String> findMaxNomeFromSalva(@Param("name") String name);



}