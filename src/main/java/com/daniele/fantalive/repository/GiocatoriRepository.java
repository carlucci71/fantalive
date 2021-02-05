package com.daniele.fantalive.repository;


import java.util.List;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.daniele.fantalive.entity.Giocatori;


public interface GiocatoriRepository extends CrudRepository<Giocatori, Integer> {
	
	String query = "select id,squadra,nome,ruolo,macroRuolo,quotazione, dataNascita from Giocatori g where not exists (select 1 from Fantarose f where g.id=f.idGiocatore)";
	
	@Query(query)
	List<Object[]> getGiocatoriLiberi();

	@Query(query)
	Page<Object[]> getGiocatoriLiberiPaginati(Pageable pageable);
	
//	Iterable<Giocatori>  getGiocatoriLiberi();
	
}