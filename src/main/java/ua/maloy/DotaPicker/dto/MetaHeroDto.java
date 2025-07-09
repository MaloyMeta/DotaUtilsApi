package ua.maloy.DotaPicker.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetaHeroDto {
    private long heroId;
    private String localizedName;
    private double winrate;
}
