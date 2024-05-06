package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SerieRepository extends JpaRepository<Serie, UUID> {
    Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);

    List<Serie> findbyAtoresContainingIgnoreCase(String nomeAtor);

    List<Serie> findbyAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(String nomeAtor, String avaliacao);

    List<Serie> findbyTop5OrderByAvaliacaoDesc();

    List<Serie> findbyGenero(Categoria categoria);

    List<Serie> findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(int totalTemporadas, double avaliacao);

    //select do banco @Query(value = "select * from series WHERE series.total_temporadas <=5 AND series.avaliacao >=7.5")
    // abaixo uso do JPQL
    @Query("SELECT s from Serie s WHERE S.totalTemporadas <= :totalTemporadas AND s.avaliacao >= :avaliacao")
    List<Serie> seriesPorTemporadaEAvaliacao(int totalTemporadas, double avaliacao);

    @Query("SELECT e from Serie s JOIN s.episodios e WHERE e.titulo ILIKE %:episodioTrecho% ")
    List<Episodio> episodiosPorTrecho(String episodioTrecho);

    @Query("SELECT e from Serie s JOIN s.episodios e WHERE s = :serie ORDER BY e.avaliacao DESC LIMIT 5 ")
    List<Episodio> topEpisodiosPorSerie(Serie serie);

    @Query("SELECT e from Serie s JOIN s.episodios e WHERE s = :serie AND YEAR(e.dataLancamento) >= :anoLancamento ")
    List<Episodio> episodiosPorSerieEAno(Serie serie, int anoLancamento);
}
