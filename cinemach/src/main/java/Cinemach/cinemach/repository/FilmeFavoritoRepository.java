package Cinemach.cinemach.repository;

import Cinemach.cinemach.model.FilmeFavorito;
import Cinemach.cinemach.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilmeFavoritoRepository extends JpaRepository<FilmeFavorito, Long> {
    FilmeFavorito findByUsuarioAndImdbId(Usuario usuario, String imdbId);
    void deleteByUsuarioAndImdbId(Usuario usuario, String imdbId);
    List<FilmeFavorito> findByUsuario(Usuario usuario);
}
