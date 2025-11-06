package Cinemach.cinemach.repository;

import Cinemach.cinemach.model.FilmeSalvo;
import Cinemach.cinemach.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FilmeSalvoRepository extends JpaRepository<FilmeSalvo, Long> {
    List<FilmeSalvo> findByUsuario(Usuario usuario);
    Optional<FilmeSalvo> findByUsuarioAndImdbId(Usuario usuario, String imdbId);
    long countByUsuario(Usuario usuario);


}