package com.SWD_G4.OrderFlow.dto.response;

import com.SWD_G4.OrderFlow.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO to cache Page data in Redis
 * Since Page interface cannot be easily deserialized, we cache List and total separately
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CachedPageData implements Serializable {
    private List<Product> content;
    private Long totalElements;
    private Integer totalPages;
    private Integer pageNumber;
    private Integer pageSize;
    private Boolean first;
    private Boolean last;
}

