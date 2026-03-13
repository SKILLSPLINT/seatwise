package com.seatwise.booking_service.service;

import com.seatwise.booking_service.client.EventServiceClient;
import com.seatwise.booking_service.dto.response.SeatResponse;
import exception.BadRequestException;
import exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventServiceClient client;

    SeatResponse reserveSeat(UUID seatID, UUID eventID,UUID userId,String userEmail) {
        try {
            return client.bookSeat(seatID, eventID,userId,userEmail);
        } catch (Exception e) {
            log.error("To reserver seat failed , please try again", e);
            throw new BadRequestException("To reserver seat failed , please try again");
        }
    }

    SeatResponse confirmSeat(UUID seatID,UUID userId,String userEmail) {
        try {
            log.info("confirming seat with id {}", seatID);
            log.info("confirming  user  id {}", userId);
            return client.confirmSeat(seatID,userId,userEmail);
        } catch (Exception e) {
            log.error("To reserver seat failed , please try again", e);
            throw new BadRequestException("To reserver seat failed , please try again");
        }
    }


}
