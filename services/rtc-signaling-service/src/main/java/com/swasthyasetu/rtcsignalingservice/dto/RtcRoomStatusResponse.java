package com.swasthyasetu.rtcsignalingservice.dto;

import java.util.List;

public record RtcRoomStatusResponse(
    List<String> participants
) {
}
