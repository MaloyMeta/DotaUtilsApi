package ua.maloy.DotaPicker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ua.maloy.DotaPicker.dto.CounterPickRequest;
import ua.maloy.DotaPicker.dto.CounterPickResponse;
import ua.maloy.DotaPicker.dto.MetaHeroDto;
import ua.maloy.DotaPicker.dto.PopularItemsByHero;
import ua.maloy.DotaPicker.entity.Hero;
import ua.maloy.DotaPicker.repositories.HeroRepository;
import ua.maloy.DotaPicker.services.GetPopularItemsByHeroService;
import ua.maloy.DotaPicker.services.UpdateDataService;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@RequestMapping("/dota/api/v1")
public class DotaRestApiController {
    private final HeroRepository heroRepository;

    private final UpdateDataService heroUpdateService;

    private final GetPopularItemsByHeroService getPopularItemsByHeroService;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    @PostMapping("hero/counter-picks")
    public List<CounterPickResponse> getCounterPicks(@RequestBody CounterPickRequest request) {
        List<Hero> allHeroes = heroRepository.findAll();
        Map<Long, Hero> heroesById = allHeroes.stream()
                .collect(Collectors.toMap(Hero::getOpenDotaId, h -> h));

        List<Long> enemyIds = request.getEnemyIds();

        List<Hero> enemyHeroes = enemyIds.stream()
                .map(id -> {
                    Hero hero = heroesById.get(id);
                    if (hero == null) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Герой с id: " + id + " не существует");
                }
                            return hero;
        })
                .toList();

        List<Hero> candidates = allHeroes.stream()
                .filter(h -> !enemyIds.contains(h.getOpenDotaId()))
                .toList();

        List<CounterPickResponse> result = new ArrayList<>();

        for (Hero candidate : candidates) {
            int score = 0;

            for (Hero enemy : enemyHeroes) {
                if (candidate.getStrongAgainst() != null && candidate.getStrongAgainst().containsKey(enemy.getLocalizedName())) {
                    score += candidate.getStrongAgainst().get(enemy.getLocalizedName());
                }
                if (candidate.getWeakAgainst() != null && candidate.getWeakAgainst().containsKey(enemy.getLocalizedName())) {
                    score -= candidate.getWeakAgainst().get(enemy.getLocalizedName());
                }
            }

            result.add(new CounterPickResponse(
                    candidate.getOpenDotaId(),
                    candidate.getLocalizedName(),
                    score
            ));
        }
        return result.stream()
                .sorted(Comparator.comparingInt(CounterPickResponse::getScore).reversed())
                .toList();
    }

    @GetMapping("hero/popular-items/{hero_id}")
    public PopularItemsByHero getPopularItemByHero(@PathVariable(name = "hero_id") Long hero_id) throws JsonProcessingException {

        if (heroRepository.findById(hero_id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hero with id: " + hero_id + " not found");
        }

        String url = "https://api.opendota.com/api/heroes/" + hero_id + "/itemPopularity";
        String json = restTemplate.getForObject(url, String.class);
        JsonNode node = objectMapper.readTree(json);

        return getPopularItemsByHeroService.parsePopularItems(node);

    }

    @GetMapping("hero/meta-heroes")
    public List<MetaHeroDto> getMetaHeroes(){
        return heroRepository.findAll().stream()
                .filter(hero -> hero.getWinrate() > 55)
                .sorted((h1,h2) -> Double.compare(h2.getWinrate(), h1.getWinrate()))
                .map(hero -> new MetaHeroDto(hero.getOpenDotaId(),
                                                   hero.getLocalizedName(),
                                                   hero.getWinrate()))
                .toList();
        }

    @GetMapping("update-storage/{parameter}")
    public void updateCounterStorage(@PathVariable(name = "parameter") String param) throws JsonProcessingException {
        switch (param) {
            case "hero":
                heroUpdateService.updateHeroesMatchups();
                break;
            case "item":
                heroUpdateService.updateItems();
                break;
            case "meta":
                heroUpdateService.updateMetaHeroes();
        }

    }


}
