package ua.maloy.DotaPicker.init_db;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.maloy.DotaPicker.entity.Item;
import ua.maloy.DotaPicker.repositories.ItemRepository;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InitializeDotaItemsService {

    private final ItemRepository itemRepository;

    public void initializeItems(JsonNode jsonNode) {

        try {
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                Long dotaItemId = Long.parseLong(entry.getKey());
                String dotaItemName = entry.getValue().asText();

                Optional<Item> itemOptional = itemRepository.findByDotaItemId(dotaItemId);

                if(itemOptional.isEmpty()){
                    Item item = Item.builder()
                            .dotaItemId(dotaItemId)
                            .dotaItemName(dotaItemName)
                            .build();
                    itemRepository.save(item);
                }
            }
        } catch(Exception e){
            throw new RuntimeException("Failed to initialize items", e);
        }
    }

    public boolean shouldToInitialize(JsonNode rootNode, long countDb) {
        System.out.println("кол-во предметов в OpenDotaApi: " + rootNode.size() + ", кол-во в нашей базе: " +  countDb);
        return countDb != rootNode.size();
    }
}
