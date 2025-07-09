package ua.maloy.DotaPicker.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.maloy.DotaPicker.dto.ItemDto;
import ua.maloy.DotaPicker.dto.PopularItemsByHero;
import ua.maloy.DotaPicker.repositories.ItemRepository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetPopularItemsByHeroService {

    private final ItemRepository itemRepository;

    public PopularItemsByHero parsePopularItems(JsonNode rootNode) {
        Map<String, List<ItemDto>> stageMap = Map.of(
                "start_game_items", new ArrayList<>(),
                "early_game_items", new ArrayList<>(),
                "mid_game_items", new ArrayList<>(),
                "late_game_items", new ArrayList<>()
        );

        Iterator<Map.Entry<String, JsonNode>> stages = rootNode.fields();

        while (stages.hasNext()) {
            Map.Entry<String, JsonNode> stage = stages.next();
            String stageName = stage.getKey();

            List<ItemDto> currentList = stageMap.get(stageName);
            if (currentList == null) continue;

            JsonNode itemsNode = stage.getValue();
            Iterator<Map.Entry<String, JsonNode>> items = itemsNode.fields();

            while (items.hasNext()) {
                Map.Entry<String, JsonNode> item = items.next();
                long itemId = Long.parseLong(item.getKey());
                int popularity = item.getValue().asInt();

                if (popularity < 10) continue;

                itemRepository.findByDotaItemId(itemId).ifPresent(it -> {
                    currentList.add(ItemDto.builder()
                            .itemName(it.getDotaItemName())
                            .popularity(popularity)
                            .build());
                });
            }
        }

        stageMap.values().forEach(list ->
                list.sort((a, b) -> Integer.compare(b.getPopularity(), a.getPopularity()))
        );

        return PopularItemsByHero.builder()
                .start_game_items(stageMap.get("start_game_items"))
                .early_game_items(stageMap.get("early_game_items"))
                .mid_game_items(stageMap.get("mid_game_items"))
                .late_game_items(stageMap.get("late_game_items"))
                .build();
    }

}
