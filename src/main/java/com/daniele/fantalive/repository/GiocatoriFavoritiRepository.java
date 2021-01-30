package com.daniele.fantalive.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.daniele.fantalive.entity.GiocatoriFavoriti;
public interface GiocatoriFavoritiRepository extends CrudRepository<GiocatoriFavoriti, Integer> 
{
	
	@Query("from GiocatoriFavoriti where idGiocatore=?1 and idAllenatore =?2")
	GiocatoriFavoriti getFavorite(Integer idGiocatore, Integer idAllenatore);

	@Query("from GiocatoriFavoriti where idAllenatore =?1")
	Iterable<GiocatoriFavoriti> getListaFavoriti(Integer idAllenatore);

}