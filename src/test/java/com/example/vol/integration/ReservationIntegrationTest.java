package com.example.vol.integration;

import com.example.vol.dtos.ReservationDto;
import com.example.vol.entities.Vol;
import com.example.vol.repositories.VolRepository;
import com.example.vol.services.ReservationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class ReservationIntegrationTest {

    @Autowired
    private VolRepository volRepository;

    @Autowired
    private ReservationService reservationService;

    private static UUID volId;

    @BeforeEach
    void setupVol() {
        Vol vol = Vol.builder()
                .dateDepart(LocalDate.now())
                .dateArrivee(LocalDate.now().plusDays(1))
                .villeDepart("Paris")
                .villeArrivee("Lyon")
                .prix(150.0)
                .capacite(5)
                .placesRestantes(5)
                .tempsTrajet(90)
                .build();
        Vol saved = volRepository.save(vol);
        volId = saved.getId();
    }

    @Test
    @Order(1)
    void createVol() {
        Vol vol = Vol.builder()
                .dateDepart(LocalDate.now())
                .dateArrivee(LocalDate.now().plusDays(1))
                .villeDepart("Paris")
                .villeArrivee("Lyon")
                .prix(150.0)
                .capacite(5)
                .placesRestantes(5)
                .tempsTrajet(90) // must not be null
                .build();
        Vol saved = volRepository.save(vol);
        volId = saved.getId();
        assertNotNull(volId);
    }

    @Test
    @Order(2)
    void reservationSuccessAndOverbooking() {
        ReservationDto dto = new ReservationDto(volId, "Alice", "alice@example.com", 2);
        reservationService.createReservation(dto);

        Vol vol = volRepository.findById(volId).orElseThrow();
        assertEquals(3, vol.getPlacesRestantes());

        // Overbooking
        ReservationDto overDto = new ReservationDto(volId, "Bob", "bob@example.com", 4);
        assertThrows(com.example.vol.exceptions.PlacesInsuffisantesException.class,
                () -> reservationService.createReservation(overDto));
    }

    @Test
    @Order(3)
    void concurrentReservations() throws InterruptedException, ExecutionException {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        // Reset the flight to 5 seats for concurrency test
        Vol vol = volRepository.findById(volId).orElseThrow();
        vol.setPlacesRestantes(5);
        volRepository.save(vol);

        CountDownLatch latch = new CountDownLatch(1);

        Callable<Boolean> task = () -> {
            latch.await(); // synchronize start
            try {
                reservationService.createReservation(new ReservationDto(volId, "Concurrent", "c@example.com", 1));
                return true;
            } catch (Exception e) {
                return false;
            }
        };

        Future<Boolean>[] futures = new Future[threads];
        for (int i = 0; i < threads; i++) {
            futures[i] = executor.submit(task);
        }

        latch.countDown(); // start all threads

        int successCount = 0;
        for (Future<Boolean> f : futures) {
            if (f.get()) successCount++;
        }

        Vol updatedVol = volRepository.findById(volId).orElseThrow();
        assertEquals(0, updatedVol.getPlacesRestantes());
        assertEquals(5, successCount);

        executor.shutdown();
    }
}
