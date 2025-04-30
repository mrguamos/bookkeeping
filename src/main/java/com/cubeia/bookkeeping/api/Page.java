package com.cubeia.bookkeeping.api;

import jakarta.validation.constraints.Max;
import org.springframework.web.bind.annotation.RequestParam;

public record Page(
  @RequestParam(required = false) @Max(200) Integer limit,
  @RequestParam(required = false) @Max(200) Integer offset
) {

}
