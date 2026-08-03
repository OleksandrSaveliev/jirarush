package com.javarush.jira.bugtracking.task.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class TagTo {
    @NotBlank
    @Size(min = 2, max = 32)
    private String tag;

    public TagTo(String tag) {
        this.tag = tag;
    }
}