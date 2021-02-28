using System;
using io.unlaunch;

namespace NetCore
{
    class Program
    {
        private const string SdkKey = "prod-server-0bdc1324-cbec-4740-83e9-86045d1cd93f";
        private const string FlagKey = "promotion";
        // Name of the attribute defined in the feature flag
        private const string CountryAttributeName = "promotion_country";

        // 2% of users from these countries will get promotion
        private const string Usa = "USA";
        private const string Deu = "DEU";
        private const string Jpn = "JPN";

        private static readonly string[] Countries =
        {
            "VNM",
            "COL",
            Usa,
            Deu,
            "FRA",
            "ITA",
            Jpn
        };

        private const int NumIterations = 1000;

        static void Main(string[] args)
        {
            var client = UnlaunchClient.Create(SdkKey);

            try
            {
                client.AwaitUntilReady(TimeSpan.FromSeconds(6));
            }
            catch (TimeoutException e)
            {
                Console.WriteLine($"Client wasn't ready, {e.Message}");
            }

            PrintBanner();

            var targetedCountriesOnVariation = 0;
            var targetedCountriesOffVariation = 0;
            var offVariation = 0;
            var random = new Random();
            for (var i = 0; i < NumIterations; i++)
            {
                var country = Countries[random.Next(Countries.Length)];
                var attributes = new[]
                {
                    UnlaunchAttribute.NewSet(CountryAttributeName, new[] {country})
                };

                var feature = client.GetFeature(FlagKey, $"user{i}", attributes);
                var variation = feature.GetVariation();
                if (variation == "on")
                {
                    targetedCountriesOnVariation++;
                    var price = feature.GetVariationConfig().GetDouble(country);
                    Console.WriteLine($"The promotion price for {country} is {price}");
                }

                if (variation == "off")
                {
                    if (country == Usa || country == Deu || country == Jpn)
                    {
                        targetedCountriesOffVariation++;
                    }
                    offVariation++;
                }

                if (country != Usa && country != Deu && country != Jpn && variation == "on")
                {
                    Console.WriteLine("This won't happen");
                }
            }
            
            // We select country randomly so number of on variations would not be exactly 2% 
            // but it should be very small compare to number off variations
            Console.WriteLine($"Number of on variations for targeted countries: {targetedCountriesOnVariation}");
            Console.WriteLine($"Number of off variations for targeted countries: {targetedCountriesOffVariation}");
            Console.WriteLine($"Number of off variations for all countries: {offVariation}");

            client.Shutdown();
        }

        private static void PrintBanner()
        {
            Console.WriteLine($"Promotion in countries: {Usa} price 60, {Deu} price 50, {Jpn} price 600");
            Console.WriteLine("");
            Console.WriteLine($"Mocking {NumIterations} requests");
            Console.WriteLine("---");
        }
    }
}