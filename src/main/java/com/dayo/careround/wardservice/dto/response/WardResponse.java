package com.dayo.careround.wardservice.dto.response;

import java.util.UUID;

public record WardResponse(
   UUID publicId,
   String name,
   int capacity
) {}
