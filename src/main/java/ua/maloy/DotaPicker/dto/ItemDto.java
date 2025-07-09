package ua.maloy.DotaPicker.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDto {
    private String itemName;
    private int popularity;
}

