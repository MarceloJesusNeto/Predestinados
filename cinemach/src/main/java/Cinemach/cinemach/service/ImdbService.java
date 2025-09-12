package Cinemach.cinemach.service;

import Cinemach.cinemach.model.Filme;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ImdbService {
    private static final String API_KEY = "3f7558c4";
    private static final String SEARCH_URL = "http://www.omdbapi.com/?apikey=" + API_KEY + "&s=";
    private static final String DETAIL_URL = "http://www.omdbapi.com/?apikey=" + API_KEY + "&i=";

    private final RestTemplate restTemplate = new RestTemplate();

    // Lista de gêneros para buscar
    private static final List<String> KEYWORDS = Arrays.asList(

            "love", "life", "death", "dark", "light", "shadow", "dream", "power", "world", "time",
            "future", "past", "last", "first", "final", "rise", "fall", "legend", "story", "secret",
            "lost", "found", "return", "day", "night", "moon", "sun", "star", "sky", "sea", "ocean",
            "earth", "fire", "water", "wind", "blood", "stone", "ring", "king", "queen", "lord",
            "empire", "war", "battle", "fight", "warrior", "hero", "villain", "mission", "hunter",
            "killer", "ghost", "spirit", "soul", "angel", "demon", "devil", "monster", "beast",
            "dragon", "creature", "alien", "robot", "machine", "future", "past", "eternal", "infinite",
            "man", "woman", "boy", "girl", "child", "father", "mother", "brother", "sister",
            "family", "friend", "enemy", "stranger", "doctor", "kingdom", "city", "village", "house",
            "road", "island", "jungle", "forest", "desert", "mountain", "river", "castle",
            "horror", "terror", "thriller", "crime", "detective", "mystery", "adventure", "fantasy",
            "magic", "science", "space", "galaxy", "universe", "robot", "cyber", "matrix",
            "dreams", "nightmare", "curse", "spell", "witch", "wizard", "vampire", "zombie",
            "kiss", "heart", "romance", "tears", "promise", "hope", "destiny", "choice", "chance",
            "soldier", "sniper", "gun", "bullet", "explosion", "agent", "spy", "police", "cop",
            "detective", "chase", "escape", "prison", "fight", "survivor", "rescue", "revenge"
    );

    public List<Filme> buscarPorTitulo(String titulo) {
        String resposta = restTemplate.getForObject(SEARCH_URL + titulo, String.class);
        JSONObject json = new JSONObject(resposta);

        List<Filme> filmes = new ArrayList<>();
        if (json.has("Search")) {
            JSONArray results = json.getJSONArray("Search");

            for (int i = 0; i < results.length(); i++) {
                JSONObject f = results.getJSONObject(i);
                String imdbId = f.getString("imdbID");

                String detalheStr = restTemplate.getForObject(DETAIL_URL + imdbId, String.class);
                JSONObject detalhe = new JSONObject(detalheStr);

                String nota = detalhe.optString("imdbRating", "N/A");
                filmes.add(new Filme(
                        detalhe.getString("Title"),
                        detalhe.getString("Poster"),
                        detalhe.optString("Genre", "Desconhecido"),
                        detalhe.optString("Plot", "Sem descrição disponível"),
                        nota
                ));
            }
        }
        return filmes;
    }

    public List<Filme> buscarFilmesAleatorios() {
        List<Filme> filmes = new ArrayList<>();
        Random random = new Random();

        Collections.shuffle(KEYWORDS);
        List<String> escolhidos = KEYWORDS.subList(0, 10); // buscar mais palavras-chave pra aumentar variedade

        for (String palavra : escolhidos) {
            String resposta = restTemplate.getForObject(SEARCH_URL + palavra, String.class);
            JSONObject json = new JSONObject(resposta);

            if (json.has("Search")) {
                JSONArray results = json.getJSONArray("Search");

                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < results.length(); i++) indices.add(i);
                Collections.shuffle(indices);

                for (int idx : indices) {
                    JSONObject f = results.getJSONObject(idx);
                    String imdbId = f.getString("imdbID");

                    String detalheStr = restTemplate.getForObject(DETAIL_URL + imdbId, String.class);
                    JSONObject detalhe = new JSONObject(detalheStr);

                    String nota = detalhe.optString("imdbRating", "N/A");
                    if (!nota.equals("N/A")) {
                        filmes.add(new Filme(
                                detalhe.getString("Title"),
                                detalhe.getString("Poster"),
                                detalhe.optString("Genre", "Desconhecido"),
                                detalhe.optString("Plot", "Sem descrição disponível"),
                                nota
                        ));
                    }

                    if (filmes.size() >= 50) break; // garante estoque de filmes
                }
            }
            if (filmes.size() >= 50) break;
        }
        return filmes;
    }
}
