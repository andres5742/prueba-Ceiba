package com.btg.fondos.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "funds")
public class Fund {

    @Id
    private Integer fundId;

    private String name;
    private long minAmountCop;
    private String category;
}
