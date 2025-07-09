package ua.maloy.DotaPicker.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CounterPickRequest {
    private List<Long> enemyIds;
}
