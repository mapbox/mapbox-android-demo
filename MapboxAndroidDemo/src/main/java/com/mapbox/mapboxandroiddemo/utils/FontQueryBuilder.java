/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mapbox.mapboxandroiddemo.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Builder class for constructing a query for downloading a font.
 */
public class FontQueryBuilder {

  @NonNull
  private String familyName;

  @Nullable
  private Float width = null;

  @Nullable
  private Integer weight = null;

  @Nullable
  private Float italic = null;

  @Nullable
  private Boolean bestEffort = null;

  FontQueryBuilder(@NonNull String familyName) {
    this.familyName = familyName;
  }

  String build() {
    if (weight == null && width == null && italic == null && bestEffort == null) {
      return familyName;
    }
    StringBuilder builder = new StringBuilder();
    builder.append("name=").append(familyName);
    if (weight != null) {
      builder.append("&weight=").append(weight);
    }
    if (width != null) {
      builder.append("&width=").append(width);
    }
    if (italic != null) {
      builder.append("&italic=").append(italic);
    }
    if (bestEffort != null) {
      builder.append("&besteffort=").append(bestEffort);
    }
    return builder.toString();
  }
}
