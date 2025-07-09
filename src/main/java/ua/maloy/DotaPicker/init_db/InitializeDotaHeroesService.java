package ua.maloy.DotaPicker.init_db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.maloy.DotaPicker.entity.Hero;
import ua.maloy.DotaPicker.repositories.HeroRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InitializeDotaHeroesService {

    private final HeroRepository heroRepository;


    public void initializeHeroes(JsonNode jsonNode) throws JsonProcessingException {

        try{
            for (JsonNode h : jsonNode) {
                int openDotaId = h.get("id").asInt();
                String localizedName = h.get("localized_name").asText();

                List<String> roles = new ArrayList<>();
                JsonNode rolesNode = h.get("roles");

                if (rolesNode.isArray()) {
                    for (JsonNode role : rolesNode) {
                        roles.add(role.asText());
                    }
                }
                Optional<Hero> heroOptional = heroRepository.findByOpenDotaId(openDotaId);

                if (heroOptional.isEmpty()) {
                    Hero hero = Hero.builder()
                            .openDotaId(openDotaId)
                            .localizedName(localizedName)
                            .roles(roles)
                            .build();
                    heroRepository.saveAndFlush(hero);
                }
            }
        } catch(Exception e){
            throw new RuntimeException("Failed to initialize heroes", e);
        }
    }

    public boolean shouldToInitialize(JsonNode rootNode, long countDb) {
        System.out.println("кол-во героев в OpenDotaApi: " + rootNode.size() + ", кол-во в нашей базе: " +  countDb);
        return countDb != rootNode.size();
    }

}

