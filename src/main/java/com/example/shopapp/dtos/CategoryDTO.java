package com.example.shopapp.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    @NotBlank(message = "Pleas input value")
    private String name;
}
