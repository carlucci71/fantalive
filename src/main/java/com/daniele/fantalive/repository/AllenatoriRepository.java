package com.daniele.fantalive.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.daniele.fantalive.entity.Allenatori;
public interface AllenatoriRepository extends CrudRepository<Allenatori, Integer> 
{
	@Query("from Allenatori order by ordine")
	Iterable<Allenatori> getAllenatoriOrderByOrdine();
	
}