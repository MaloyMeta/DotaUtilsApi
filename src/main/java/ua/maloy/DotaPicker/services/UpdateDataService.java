package ua.maloy.DotaPicker.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ua.maloy.DotaPicker.dto.MetaHeroDto;
import ua.maloy.DotaPicker.entity.Hero;
import ua.maloy.DotaPicker.init_db.InitializeDotaHeroesService;
import ua.maloy.DotaPicker.init_db.InitializeDotaItemsService;
import ua.maloy.DotaPicker.repositories.HeroRepository;
import ua.maloy.DotaPicker.repositories.ItemRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UpdateDataService {
private final InitializeDotaHeroesService initializeDotaHeroesService;

    private final HeroRepository heroRepository;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    private final ItemRepository itemRepository;

    private final InitializeDotaItemsService initializeDotaItemsService;


    public void updateHeroesMatchups() throws JsonProcessingException{

        initializeHeroesIfNeeded();

        try{
            List<Hero> heroes = heroRepository.findAll();

            for( Hero h : heroes){
                String matchupUrl = "https://api.opendota.com/api/heroes/" + h.getOpenDotaId() + "/matchups";
                ResponseEntity<String> matchupResponse = restTemplate.getForEntity(matchupUrl, String.class);
                JsonNode matchups = objectMapper.readTree(matchupResponse.getBody());

                Map<String,Integer> strongAgainst = new HashMap<>();
                Map<String,Integer> weakAgainst = new HashMap<>();

                for(JsonNode matchup : matchups){
                    long enemyOpenDotaId =  matchup.get("hero_id").asInt();
                    int gamesPlayed =  matchup.get("games_played").asInt();
                    int wins = matchup.get("wins").asInt();

                    if (gamesPlayed < 30 ){
                        continue;
                    }

                    double winrate = (double) wins / gamesPlayed;
                    double diff = winrate - 0.5;

                    if (Math.abs(diff) < 0.02) continue; // незначительное отличие — игнорируем

                    double confidence = Math.log(gamesPlayed);
                    double rawScore = diff * 200 * (confidence / 6.0);
                    int score = (int) Math.round(rawScore);


                    Optional<Hero> enemyOptional = heroRepository.findByOpenDotaId(enemyOpenDotaId);
                    if(enemyOptional.isPresent()){
                        String enemyName = enemyOptional.get().getLocalizedName();
                        if (diff > 0){
                            strongAgainst.put(enemyName,score);
                        } else {
                            weakAgainst.put(enemyName, score);
                        }
                    }

                }

                Hero hero = h;

                hero.setWeakAgainst(weakAgainst);
                hero.setStrongAgainst(strongAgainst);

                heroRepository.saveAndFlush(hero);
            }
        } catch(Exception e){
            throw new RuntimeException("Something went wrong while updating heroes", e);
        }
    }

    public void updateMetaHeroes() throws JsonProcessingException{
        String url = "https://api.opendota.com/api/heroStats";
        String response = restTemplate.getForObject(url, String.class);
        JsonNode node = objectMapper.readTree(response);

        for(JsonNode hero : node){
            long heroId = hero.get("id").asLong();
            int proPick =  hero.get("pro_pick").asInt();
            int proWin =  hero.get("pro_win").asInt();
            int pubPick = hero.get("pub_pick").asInt();
            int pubWin = hero.get("pub_win").asInt();
            double winrate;
            Optional<Hero> heroOpt = heroRepository.findByOpenDotaId(heroId);

            if (heroOpt.isPresent()) {
                Hero currentHero = heroOpt.get();

                if (proPick <= 50){
                    winrate = ((double) pubWin / pubPick) * 100;
                    currentHero.setWinrate(winrate);

                } else {
                    winrate = ((double) proWin / proPick) * 100;
                    currentHero.setWinrate(winrate);

                }

                heroRepository.saveAndFlush(currentHero);
            }

        }
    }

    public void updateItems() throws JsonProcessingException{
        String itemUrl = "https://api.opendota.com/api/constants/item_ids";

        String response = restTemplate.getForObject(itemUrl, String.class);

        JsonNode itemsFromApi = objectMapper.readTree(response);

        long countInDb = itemRepository.count();

        // Проверка: если в базе нет героев или их меньше, чем в API
        if (initializeDotaItemsService.shouldToInitialize(itemsFromApi,countInDb)) {
            initializeDotaItemsService.initializeItems(itemsFromApi);
        }
    }

    public void initializeHeroesIfNeeded() throws JsonProcessingException{

        String url = "https://api.opendota.com/api/heroes";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        JsonNode heroesFromApi = objectMapper.readTree(response.getBody());

        long countInDb = heroRepository.count();

        // Проверка: если в базе нет героев или их меньше, чем в API
        if (initializeDotaHeroesService.shouldToInitialize(heroesFromApi,countInDb)) {
            initializeDotaHeroesService.initializeHeroes(heroesFromApi);
        }
    }
}
