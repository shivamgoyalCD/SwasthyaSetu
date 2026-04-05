package com.swasthyasetu.rtcsignalingservice.api;

import com.swasthyasetu.rtcsignalingservice.dto.CreateRtcRoomResponse;
import com.swasthyasetu.rtcsignalingservice.dto.RtcRoomStatusResponse;
import com.swasthyasetu.rtcsignalingservice.service.RtcRoomService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class RtcRoomController {
  private final RtcRoomService rtcRoomService;

  @PostMapping("/rtc/room/create")
  public CreateRtcRoomResponse createRoom(@RequestParam("appointmentId") UUID appointmentId) {
    return rtcRoomService.createRoom(appointmentId);
  }

  @GetMapping("/rtc/room/{appointmentId}/status")
  public RtcRoomStatusResponse getRoomStatus(@PathVariable("appointmentId") UUID appointmentId) {
    return rtcRoomService.getRoomStatus(appointmentId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RTC room not found"));
  }
}
