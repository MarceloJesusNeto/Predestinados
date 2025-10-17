package Cinemach.cinemach.service;

import Cinemach.cinemach.model.Filme;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;

@Service
public class ImdbService {
    private static final String API_KEY = "fb475869";
    private static final String SEARCH_URL = "https://www.omdbapi.com/?apikey=" + API_KEY + "&s=";
    private static final String DETAIL_URL = "https://www.omdbapi.com/?apikey=" + API_KEY + "&i=";

    private final RestTemplate restTemplate = new RestTemplate();

    private static final List<String> KEYWORDS = Arrays.asList(
            "love","life","death","dark","dream","power","world","future","past","legend","secret","lost","return",
            "day","night","moon","sun","star","ocean","fire","king","queen","war","battle","hero","villain",
            "ghost","soul","monster","alien","robot","magic","space","universe","vampire","zombie","romance"
    );

    private String reduzirPoster(String posterUrl) {
        if (posterUrl == null || posterUrl.equals("N/A")) return posterUrl;
        return posterUrl.replaceAll("\\._V1.*\\.jpg", "._V1_SX300.jpg");
    }

    public List<Filme> buscarPorTitulo(String titulo) {
        try {
            String resposta = restTemplate.getForObject(SEARCH_URL + titulo, String.class);
            JSONObject json = new JSONObject(resposta);

            List<Filme> filmes = new ArrayList<>();
            if (json.has("Search")) {
                JSONArray results = json.getJSONArray("Search");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject f = results.getJSONObject(i);
                    String imdbId = f.optString("imdbID", "");

                    // já busca detalhes
                    Filme detalhes = buscarDetalhes(imdbId);
                    if (detalhes != null) {
                        filmes.add(detalhes);
                    }
                }
            }
            for (Filme f : filmes) {
                System.out.println("DEBUG >>> " + f.getTitulo() +
                        " | Genero: " + f.getGenero() +
                        " | Nota: " + f.getNota());
            }
            return filmes;
        } catch (Exception e) {
            System.err.println("Erro buscarPorTitulo: " + e.getMessage());
            return Collections.emptyList();
        }
    }


    public List<Filme> buscarFilmesAleatorios() {
        List<Filme> filmes = new ArrayList<>();
        try {
            Collections.shuffle(KEYWORDS);
            List<String> escolhidos = KEYWORDS.subList(0, 5);

            for (String palavra : escolhidos) {
                String resposta = restTemplate.getForObject(SEARCH_URL + palavra, String.class);
                JSONObject json = new JSONObject(resposta);

                if (json.has("Search")) {
                    JSONArray results = json.getJSONArray("Search");

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject f = results.getJSONObject(i);
                        String imdbId = f.optString("imdbID", "");

                        Filme detalhes = buscarDetalhes(imdbId);
                        if (detalhes != null) {
                            filmes.add(detalhes);
                        }
                        if (filmes.size() >= 20) break;
                    }
                }
                if (filmes.size() >= 20) break;
            }
        } catch (Exception e) {
            System.err.println("Erro buscarFilmesAleatorios: " + e.getMessage());
        }
        return filmes;
    }


    public Filme buscarDetalhes(String imdbId) {
        try {
            String detalheStr = restTemplate.getForObject(DETAIL_URL + imdbId, String.class);
            JSONObject detalhe = new JSONObject(detalheStr);

            return new Filme(
                    detalhe.optString("Title", "Sem título"),
                    reduzirPoster(detalhe.optString("Poster", "")),
                    detalhe.optString("Genre", "Desconhecido"),
                    detalhe.optString("Plot", "Sem descrição disponível"),
                    detalhe.optString("imdbRating", "N/A"),
                    imdbId
            );
        } catch (Exception e) {
            System.err.println("Erro buscarDetalhes: " + e.getMessage());
            return null;
        }
    }
}
