package com.daniele.fantalive.repository;

import org.springframework.data.repository.CrudRepository;

import com.daniele.fantalive.entity.LoggerMessaggi;
public interface LoggerRepository extends CrudRepository<LoggerMessaggi, Integer> 
{
	
}