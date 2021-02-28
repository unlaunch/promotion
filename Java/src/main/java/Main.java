import io.unlaunch.UnlaunchAttribute;
import io.unlaunch.UnlaunchClient;
import io.unlaunch.UnlaunchFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {

    private static final String SDK_KEY = "prod-server-0bdc1324-cbec-4740-83e9-86045d1cd93f";

    private static final String FLAG_KEY = "promotion";

    // Name of the attribute defined in the feature flag
    private static final String COUNTRY_ATTRIBUTE_NAME = "promotion_country";

    // Users from these countries will use NEW payment gateway
    private static final String USA = "USA";
    private static final String DEU = "DEU";
    private static final String JPN = "JPN";

    private static final String[] COUNTRIES = {
            USA,
            "VNM",
            "COL",
            JPN,
            "FRA",
            "ITA",
            DEU
    };

    // Number of time to iterate and generate random users
    private static final int NUM_ITERATIONS = 1000;

    public static void main(String[] args) {
        final UnlaunchClient client = UnlaunchClient.create(SDK_KEY);

        try {
            client.awaitUntilReady(6, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            System.out.println("Client wasn't ready, " + e.getMessage());
        }

        printBanner();

        int targetedCountriesOnVariation = 0;
        int targetedCountriesOffVariation = 0;
        int offVariation = 0;
        final Random Random = new Random();
        // Randomly generate userIds and countries to mock request
        for (int i = 0; i < NUM_ITERATIONS ; i++)
        {
            final String country = COUNTRIES[Random.nextInt(COUNTRIES.length)];
            final List<String> countrySet = new ArrayList<>();
            countrySet.add(country);

            final UnlaunchFeature feature = client.getFeature(FLAG_KEY,
                    "user" + i,
                    UnlaunchAttribute.newSet(COUNTRY_ATTRIBUTE_NAME, countrySet));

            final String variation = feature.getVariation();
            if ("on".equals(variation))
            {
                targetedCountriesOnVariation++;

                double price = feature.getVariationConfig().getDouble(country);
                System.out.println("The promotion price for " + country + " is " + price);
            }

            if ("off".equals(variation))
            {
                if (USA.equals(country) || DEU.equals(country) || JPN.equals(country))
                {
                    targetedCountriesOffVariation++;
                }
                offVariation++;
            }

            if (!USA.equals(country) && !DEU.equals(country) && !JPN.equals(country) && "on".equals(variation))
            {
                System.out.println("This won't happen");
            }
        }

        // We select country randomly so number of on variations would not be exactly 2%
        // but it should be very small compare to number of variations
        System.out.println("Number of on variations for targeted countries: " + targetedCountriesOnVariation);
        System.out.println("Number of off variations for targeted countries: " + targetedCountriesOffVariation);
        System.out.println("Number of off variations for all countries: " + offVariation);

        client.shutdown();
    }

    private static void printBanner() {
        System.out.println("Promotion in countries: " + USA + " price 60, " + DEU + " price 50, " + JPN + " price 600");
        System.out.println("");
        System.out.println("Mocking " + NUM_ITERATIONS  + " requests");
        System.out.println("---");
    }
}
