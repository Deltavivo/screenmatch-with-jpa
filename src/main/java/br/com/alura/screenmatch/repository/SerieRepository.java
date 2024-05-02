package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SerieRepository extends JpaRepository<Serie, UUID> {

}
