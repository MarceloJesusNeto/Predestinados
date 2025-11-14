package Cinemach.cinemach.service;

import Cinemach.cinemach.model.Filme;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ImdbService {
    private static final String API_KEY = "4bd65d1e";
    private static final String SEARCH_URL = "https://www.omdbapi.com/?apikey=" + API_KEY + "&s=";
    private static final String DETAIL_URL = "https://www.omdbapi.com/?apikey=" + API_KEY + "&i=";

    private final RestTemplate restTemplate = new RestTemplate();
    private static final ExecutorService executor = Executors.newFixedThreadPool(12);

    private static final List<String> KEYWORDS = Arrays.asList(
            "love","life","death","dream","power","world","future","legend",
            "night","sun","moon","fire","war","battle","hero","villain",
            "ghost","soul","magic","space","vampire","zombie","romance"
    );

    private List<Filme> cacheFilmesAleatorios = new ArrayList<>();
    private long cacheTimestamp = 0;
    private static final long CACHE_TTL = 1000L * 60 * 60;

    private String reduzirPoster(String posterUrl) {
        if (posterUrl == null || posterUrl.equals("N/A")) return "/img/placeholder.jpg";
        return posterUrl.replaceAll("\\._V1.*\\.jpg", "._V1_SX150.jpg");
    }

    public List<Filme> buscarFilmesAleatorios() {
        long agora = System.currentTimeMillis();
        if (!cacheFilmesAleatorios.isEmpty() && (agora - cacheTimestamp < CACHE_TTL)) {
            return cacheFilmesAleatorios;
        }

        List<Filme> filmes = Collections.synchronizedList(new ArrayList<>());
        Set<String> titulosUsados = ConcurrentHashMap.newKeySet();

        List<String> palavrasMisturadas = new ArrayList<>(KEYWORDS);
        Collections.shuffle(palavrasMisturadas);


        List<String> palavrasSelecionadas = palavrasMisturadas.subList(0, Math.min(10, palavrasMisturadas.size()));

        List<CompletableFuture<Void>> futures = palavrasSelecionadas.stream()
                .map(palavra -> CompletableFuture.runAsync(() -> {
                    try {
                        String resposta = restTemplate.getForObject(SEARCH_URL + palavra, String.class);
                        JSONObject json = new JSONObject(resposta);

                        if (json.has("Search")) {
                            JSONArray results = json.getJSONArray("Search");


                            for (int i = 0; i < Math.min(results.length(), 10); i++) {
                                JSONObject f = results.getJSONObject(i);
                                String imdbId = f.optString("imdbID", "");


                                CompletableFuture.runAsync(() -> {
                                    Filme detalhes = buscarDetalhes(imdbId);
                                    if (detalhes != null) {
                                        String tituloLower = detalhes.getTitulo().toLowerCase();
                                        if (titulosUsados.add(tituloLower)) {
                                            filmes.add(detalhes);
                                        }
                                    }
                                }, executor);
                            }
                        }
                    } catch (Exception ignored) {}
                }, executor))
                .collect(Collectors.toList());


        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).orTimeout(20, TimeUnit.SECONDS).join();

        Collections.shuffle(filmes);
        cacheFilmesAleatorios = filmes.stream().limit(81).collect(Collectors.toList());
        cacheTimestamp = agora;
        return cacheFilmesAleatorios;
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


                List<CompletableFuture<Void>> futures = new ArrayList<>();

                for (int i = 0; i < results.length(); i++) {
                    JSONObject f = results.getJSONObject(i);
                    String imdbId = f.optString("imdbID", "");
                    futures.add(CompletableFuture.runAsync(() -> {
                        Filme detalhes = buscarDetalhes(imdbId);
                        if (detalhes != null) filmes.add(detalhes);
                    }, executor));
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }
            return filmes;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
