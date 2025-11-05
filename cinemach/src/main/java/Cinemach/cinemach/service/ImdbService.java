

package Cinemach.cinemach.service;

import Cinemach.cinemach.model.Filme;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;

@Service
public class ImdbService {
    private static final String API_KEY = "bbb4983";
    private static final String SEARCH_URL = "https://www.omdbapi.com/?apikey=" + API_KEY + "&s=";
    private static final String DETAIL_URL = "https://www.omdbapi.com/?apikey=" + API_KEY + "&i=";

    private final RestTemplate restTemplate = new RestTemplate();

    private static final List<String> KEYWORDS = Arrays.asList(
            "love","life","death","dream","power","world","future","legend",
            "night","sun","moon","fire","war","battle","hero","villain",
            "ghost","soul","magic","space","vampire","zombie","romance"
    );


    private List<Filme> cacheFilmesAleatorios = new ArrayList<>();
    private long cacheTimestamp = 0;
    private static final long CACHE_TTL = 1000L * 60 * 60; // 1h

    private String reduzirPoster(String posterUrl) {
        if (posterUrl == null || posterUrl.equals("N/A")) return "/img/placeholder.jpg";
        return posterUrl.replaceAll("\\._V1.*\\.jpg", "._V1_SX300.jpg");
    }

    public List<Filme> buscarFilmesAleatorios() {
        List<Filme> filmes = new ArrayList<>();
        Set<String> titulosUsados = new HashSet<>();

        try {
            List<String> palavrasMisturadas = new ArrayList<>(KEYWORDS);
            Collections.shuffle(palavrasMisturadas);

            for (String palavra : palavrasMisturadas) {
                String resposta = restTemplate.getForObject(SEARCH_URL + palavra, String.class);
                JSONObject json = new JSONObject(resposta);

                if (json.has("Search")) {
                    JSONArray results = json.getJSONArray("Search");

                    // agora pega até 5 filmes por palavra-chave
                    for (int i = 0; i < Math.min(results.length(), 5); i++) {
                        JSONObject f = results.getJSONObject(i);
                        String imdbId = f.optString("imdbID", "");

                        Filme detalhes = buscarDetalhes(imdbId);
                        if (detalhes != null) {
                            String tituloLower = detalhes.getTitulo().toLowerCase();

                            if (!titulosUsados.contains(tituloLower)) {
                                filmes.add(detalhes);
                                titulosUsados.add(tituloLower);
                            }
                        }

                        // agora gera até 81 filmes no cache
                        if (filmes.size() >= 81) break;
                    }
                }

                if (filmes.size() >= 81) break;
            }

            Collections.shuffle(filmes);
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
                    Filme detalhes = buscarDetalhes(imdbId);
                    if (detalhes != null) filmes.add(detalhes);
                }
            }
            return filmes;
        } catch (Exception e) {
            System.err.println("Erro buscarPorTitulo: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}