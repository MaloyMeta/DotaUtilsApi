package ua.maloy.DotaPicker.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CounterPickResponse {
    private Long heroId;
    private String localizedName;
    private int score;
}
