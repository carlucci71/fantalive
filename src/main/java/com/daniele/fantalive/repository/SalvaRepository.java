package com.daniele.fantalive.repository;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.daniele.fantalive.entity.Salva;


public interface SalvaRepository extends CrudRepository<Salva, String> {

	@Query(value = "select * from salva where nome like :name", nativeQuery = true)
	List<Salva> findSimulazioniName(@Param("name") String name);



}