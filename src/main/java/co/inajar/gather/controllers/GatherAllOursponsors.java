package co.inajar.gather.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import static co.inajar.gather.controllers.GatherAllOursponsors.Rep.concatenate;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;

@RestController
@Slf4j
@RequiredArgsConstructor
public class GatherAllOursponsors {

    class Rep {

        public static String concatenate(String senator, String congress) {
            String rep = senator + congress;
            return rep;
        }
    }

    private String getSenators() {
        return "Senators";
    }

    private String getCongress() {
        return "Congress";
    }

    @GetMapping("/gather-all-oursponsors")
    public String virtual() {

        // gather senators
        log.info("gathering all sponsors, start on {}", Thread.currentThread());
        System.out.println("gathering senators");

        CompletableFuture<String> senatorFuture = supplyAsync(this::getSenators, newVirtualThreadPerTaskExecutor());
        CompletableFuture<String> congressFuture = supplyAsync(this::getCongress, newVirtualThreadPerTaskExecutor());

        String result = concatenate(senatorFuture.join(), congressFuture.join());
        log.info("returning: {}", result, Thread.currentThread());
        return result;
    }

}