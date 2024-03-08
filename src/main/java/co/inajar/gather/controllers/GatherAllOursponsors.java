package co.inajar.gather.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
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

    private int getNumberOfThreads() { return ManagementFactory.getThreadMXBean().getThreadCount(); }

    @GetMapping("/gather-all-oursponsors")
    public String virtual() {

        // gather senators
        log.info("gathering all sponsors, start on {}", Thread.currentThread());
        log.info("Parent thread is {}", Thread.currentThread().getThreadGroup().getName());

        log.info("=== STEP1: ProPublica REST APIs start here === threadcount: {}", getNumberOfThreads());
        System.out.println("gathering representatives");
        var senators = getSenators();
        var congress = getCongress();

        // This is where we would use the SYNC senators and congress results as
        // parameters into our CompletableFutures below
        log.info("=== STEP2: Fork Node parallel Open Secrets and FEC REST APIs start here === threadcount: {}", getNumberOfThreads());
        // for node kicks off download of open secrets: campaign, sectors, contributors. FEC: campaign
        CompletableFuture<String> senatorOpenSecretsCampaignFuture = supplyAsync(this::getOpenSecretsCampaign, newVirtualThreadPerTaskExecutor());
        CompletableFuture<String> senatorOpenSecretsSectorsFuture = supplyAsync(this::getOpenSecretsSectors, newVirtualThreadPerTaskExecutor());
        CompletableFuture<String> senatorOpenSecretsContributorsFuture = supplyAsync(this::getOpenSecretsContributors, newVirtualThreadPerTaskExecutor());
        CompletableFuture<String> senatorFecCampaignFuture = supplyAsync(this::getFecCampaign, newVirtualThreadPerTaskExecutor());

        log.info("=== We should have threads here === threadcount: {}", getNumberOfThreads());
        // for node kicks off download of open secrets: campaign, sectors, contributors. FEC: campaign
        CompletableFuture<String> congressOpenSecretsCampaignFuture = supplyAsync(this::getOpenSecretsCampaign, newVirtualThreadPerTaskExecutor());
        CompletableFuture<String> congressOpenSecretsSectorsFuture = supplyAsync(this::getOpenSecretsSectors, newVirtualThreadPerTaskExecutor());
        CompletableFuture<String> congressOpenSecretsContributorsFuture = supplyAsync(this::getOpenSecretsContributors, newVirtualThreadPerTaskExecutor());
        CompletableFuture<String> congressFecCampaignFuture = supplyAsync(this::getFecCampaign, newVirtualThreadPerTaskExecutor());

        log.info("=== STEP3: Join Node Open Secrets and FEC results start here === threadcount: {}", getNumberOfThreads());
        var sosCampaignF = senatorOpenSecretsCampaignFuture.join();
        var sosSectorsF = senatorOpenSecretsSectorsFuture.join();
        var sosContributorsF = senatorOpenSecretsContributorsFuture.join();
        var sfCampaignF = senatorFecCampaignFuture.join();

        var cosCampaignF = congressOpenSecretsCampaignFuture.join();
        var cosSectorsF = congressOpenSecretsSectorsFuture.join();
        var cosContributorsF = congressOpenSecretsContributorsFuture.join();
        var cfCampaignF = congressFecCampaignFuture.join();

        log.info("=== STEP4: Merge Node data from our Join Node starts here === threadcount: {}", getNumberOfThreads());
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

        log.info("returning: {}", result, Thread.currentThread());
        return result;
    }

}