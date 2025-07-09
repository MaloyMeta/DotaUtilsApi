package ua.maloy.DotaPicker.dto;

import lombok.*;
import ua.maloy.DotaPicker.entity.Item;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PopularItemsByHero {
    private List<ItemDto> start_game_items;
    private List<ItemDto> early_game_items;
    private List<ItemDto> mid_game_items;
    private List<ItemDto> late_game_items;
}
