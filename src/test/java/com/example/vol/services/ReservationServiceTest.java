package com.example.vol.services;

import com.example.vol.dtos.ReservationDto;
import com.example.vol.entities.Reservation;
import com.example.vol.entities.Vol;
import com.example.vol.events.ReservationFailedEvent;
import com.example.vol.events.ReservationSuccessEvent;
import com.example.vol.exceptions.PlacesInsuffisantesException;
import com.example.vol.exceptions.VolNotFoundException;
import com.example.vol.repositories.ReservationRepository;
import com.example.vol.repositories.VolRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class ReservationServiceTest {

    @InjectMocks
    private ReservationService service;

    @Mock
    private VolRepository volRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterAll
    static void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void testReservationSuccess() {
        // Arrange
        UUID volId = UUID.randomUUID();
        Vol vol = Vol.builder()
                .id(volId)
                .placesRestantes(5)
                .prix(150.0)
                .tempsTrajet(120)
                .dateDepart(LocalDate.now())
                .dateArrivee(LocalDate.now().plusDays(1))
                .villeDepart("Paris")
                .villeArrivee("Lyon")
                .build();
        ReservationDto dto = new ReservationDto(volId, "John", "john@example.com", 2);

        when(volRepository.findById(volId)).thenReturn(Optional.of(vol));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Reservation reservation = service.createReservation(dto);

        // Assert
        assertNotNull(reservation);
        assertEquals(2, reservation.getNombrePlaces());
        assertEquals(3, vol.getPlacesRestantes());
        assertEquals("john@example.com", reservation.getEmail());
        assertEquals("John", reservation.getNom());

        // Verify repository calls
        verify(volRepository).save(vol);
        verify(reservationRepository).save(any(Reservation.class));

        // Verify success event
        ArgumentCaptor<ReservationSuccessEvent> eventCaptor = ArgumentCaptor.forClass(ReservationSuccessEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ReservationSuccessEvent event = eventCaptor.getValue();
        assertEquals(5, event.getPlacesDisponiblesAvant());
        assertEquals(dto, event.getDto());
    }

    @Test
    void testReservationVolNotFound() {
        // Arrange
        UUID volId = UUID.randomUUID();
        ReservationDto dto = new ReservationDto(volId, "John", "john@example.com", 2);
        when(volRepository.findById(volId)).thenReturn(Optional.empty());

        // Act & Assert
        VolNotFoundException exception = assertThrows(VolNotFoundException.class,
            () -> service.createReservation(dto));

        // Verify failure event
        ArgumentCaptor<ReservationFailedEvent> eventCaptor = ArgumentCaptor.forClass(ReservationFailedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ReservationFailedEvent event = eventCaptor.getValue();
        assertEquals(volId.toString(), event.getVolId());
        assertEquals("john@example.com", event.getEmail());
        assertTrue(event.getMessageErreur().contains("not found"));
    }

    @Test
    void testReservationPlacesInsuffisantes() {
        // Arrange
        UUID volId = UUID.randomUUID();
        Vol vol = Vol.builder()
                .id(volId)
                .placesRestantes(1)
                .prix(150.0)
                .tempsTrajet(120)
                .dateDepart(LocalDate.now())
                .dateArrivee(LocalDate.now().plusDays(1))
                .villeDepart("Paris")
                .villeArrivee("Lyon")
                .build();
        ReservationDto dto = new ReservationDto(volId, "John", "john@example.com", 2);

        when(volRepository.findById(volId)).thenReturn(Optional.of(vol));

        // Act & Assert
        PlacesInsuffisantesException exception = assertThrows(PlacesInsuffisantesException.class,
            () -> service.createReservation(dto));

        // Verify failure event
        ArgumentCaptor<ReservationFailedEvent> eventCaptor = ArgumentCaptor.forClass(ReservationFailedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ReservationFailedEvent event = eventCaptor.getValue();
        assertEquals(1, event.getPlacesDisponiblesAvant());
        assertEquals(2, event.getPlacesDemandees());
        assertTrue(event.getMessageErreur().contains("Not enough seats"));
    }
}
