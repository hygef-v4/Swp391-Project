package org.swp391.hotelbookingsystem.model;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String cityName;
    private String imageUrl;
}