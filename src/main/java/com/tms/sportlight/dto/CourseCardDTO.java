package com.tms.sportlight.dto;

import com.tms.sportlight.domain.Category;
import com.tms.sportlight.domain.CourseLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCardDTO {

  private Integer id;
  private String nickname;
  private String title;
  private String address;
  private double tuition;
  private double discountRate;
  private int time;
  private CourseLevel level;
  private String category;
  private double rating;
  private int reviewCount;

}
