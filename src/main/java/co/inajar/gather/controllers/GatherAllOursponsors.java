package co.inajar.gather.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    // Note: these are a serious oversimplification because there probably
    //      needs to be a specific method for each rep type

    private String getOpenSecretsCampaign() { return "Open Secrets Campaign"; }
    private String getOpenSecretsSectors() { return "Open Secrets Sectors"; }
    private String getOpenSecretsContributors() { return "Open Secrets Contributors"; }
    private String getFecCampaign() { return "FEC Campaign"; }

    @GetMapping("/gather-all-oursponsors")
    public String virtual() {

        // gather senators
        log.info("gathering all sponsors, start on {}", Thread.currentThread());

        log.info("=== STEP1: ProPublica REST APIs start here ===");
        System.out.println("gathering senators");
        var senators = getSenators();

        // for node kicks off download of open secrets: campaign, sectors, contributors. FEC: campaign
        CompletableFuture<String> senatorOpenSecretsCampaignFuture = supplyAsync(this::getOpenSecretsCampaign, newVirtualThreadPerTaskExecutor());
        CompletableFuture<String> senatorOpenSecretsSectorsFuture = supplyAsync(this::getOpenSecretsSectors, newVirtualThreadPerTaskExecutor());
        CompletableFuture<String> senatorOpenSecretsContributorsFuture = supplyAsync(this::getOpenSecretsContributors, newVirtualThreadPerTaskExecutor());
        CompletableFuture<String> senatorFecCampaignFuture = supplyAsync(this::getFecCampaign, newVirtualThreadPerTaskExecutor());


        System.out.println("gathering congress");
        var congress = getCongress();

        // for node kicks off download of open secrets: campaign, sectors, contributors. FEC: campaign
        CompletableFuture<String> congressOpenSecretsCampaignFuture = supplyAsync(this::getOpenSecretsCampaign, newVirtualThreadPerTaskExecutor());
        CompletableFuture<String> congressOpenSecretsSectorsFuture = supplyAsync(this::getOpenSecretsSectors, newVirtualThreadPerTaskExecutor());
        CompletableFuture<String> congressOpenSecretsContributorsFuture = supplyAsync(this::getOpenSecretsContributors, newVirtualThreadPerTaskExecutor());
        CompletableFuture<String> congressFecCampaignFuture = supplyAsync(this::getFecCampaign, newVirtualThreadPerTaskExecutor());

        var sosCampaignF = senatorOpenSecretsCampaignFuture.join();
        var sosSectorsF = senatorOpenSecretsSectorsFuture.join();
        var sosContributorsF = senatorOpenSecretsContributorsFuture.join();
        var sfCampaignF = senatorFecCampaignFuture.join();

        var cosCampaignF = congressOpenSecretsCampaignFuture.join();
        var cosSectorsF = congressOpenSecretsSectorsFuture.join();
        var cosContributorsF = congressOpenSecretsContributorsFuture.join();
        var cfCampaignF = congressFecCampaignFuture.join();

        String result =
            sosCampaignF
                .concat(sosCampaignF)
                .concat(sosSectorsF)
                .concat(sosContributorsF)
                .concat(sfCampaignF)

                .concat(cosCampaignF)
                .concat(cosSectorsF)
                .concat(cosContributorsF)
                .concat(cfCampaignF);

//        String result = concatenate(senatorOpenSecretsCampaignFuture.join(), senatorOpenSecretsSectorsFuture.join(), senatorOpenSecretsContributorsFuture.join(),
//                senatorFecCampaignFuture.join(), congressOpenSecretsCampaignFuture.join(), congressOpenSecretsSectorsFuture.join(),
//                congressOpenSecretsContributorsFuture.join(), congressFecCampaignFuture.join());
        log.info("returning: {}", result, Thread.currentThread());
        return result;
    }

}