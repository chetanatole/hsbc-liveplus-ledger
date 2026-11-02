# Merchant categories

The app includes **119 merchant names and keywords** for provisional category suggestions. They identify likely business types, not verified bank MCCs or guaranteed cashback. Built-in suggestions appear on transactions and can be reviewed there.

## Your mappings

Open **Settings → Merchant categories → Manage merchant categories** to view only your remembered and manually added mappings. Use **Add mapping** to create an exact-name mapping, or tap a saved mapping to edit, rename or delete it. You can also save a mapping with **Remember this merchant’s category** when editing a purchase.

Saved mappings take priority over built-in suggestions for newly captured alerts and pasted purchases. Names ignore capitalization, leading/trailing spaces and repeated spaces; punctuation differences require separate saved entries. Changes do not recategorize existing transactions. Delete a saved mapping to use built-in suggestions again.

Mappings are stored on the phone. They are currently not included in JSON backups.

## Built-in matching

- Match recognizable merchant names and keywords across case and punctuation variations, while avoiding matches inside unrelated words.
- Apply explicit exclusion hints first, then the Amazon/Flipkart/Myntra base-rate exceptions and the dated optional Myntra offer.
- Check specific grocery services before broader dining names: **Swiggy Instamart → Groceries**, while **Swiggy → Dining / food delivery**.
- Leave generic gateway/platform names, payment-bank descriptors and unidentified merchants for manual review. A gateway-prefixed descriptor can still match if it includes a recognizable merchant name.
- A similar displayed name can carry different MCCs. Review remembered categories when the payment route or business type changes.

Business-name aliases include [Blink Commerce → Blinkit](https://blinkit.com/contact), [Delightful Gourmet → Licious](https://www.licious.in/), [Infiniti Retail → Croma](https://www.croma.com/about-croma) and [Bharti Airtel → Airtel](https://www.airtel.in/mobile/terms-conditions). These sources establish the business identity, not the MCC or reward eligibility of a particular payment.

## Bundled catalog

| Suggested category | Names and keywords |
| --- | --- |
| Dining / food delivery | Swiggy, Zomato, restaurant, restaurants, cafe, coffee, Domino, Dominos, Domino's, Jubilant Foodworks, McDonald, McDonalds, McDonald's, Hardcastle Restaurants, Westlife Foodworld, Burger King, Pizza Hut, KFC, Sapphire Foods, Devyani International, Starbucks, Subway, Barbeque Nation, Haldiram, Haldirams, Bikanervala, Wow Momo, Rebel Foods, Faasos, Behrouz, EatClub, Box8, Mojo Pizza, Chaayos, Chai Point |
| Groceries | Blinkit, Blink Commerce, BlinkCommerce, Grofers, Instamart, BigBasket, Big Basket, Zepto, KiranaKart, Kirana Kart, Licious, Delightful Gourmet, FreshToHome, Fresh to Home, Country Delight, DMart, D Mart, Avenue Supermarts, supermarket, grocer, grocery, groceries |
| Shopping | Croma, Infiniti Retail, Reliance Digital, Vijay Sales, Decathlon, Shoppers Stop, Pantaloons, Westside, Uniqlo, H&M, Hennes Mauritz, Max Fashion, Lifestyle Stores, Nykaa, FSN E Commerce |
| Utilities | Airtel, Bharti Airtel, Reliance Jio, Jio Infocomm, Jio Fiber, JioFiber, Vodafone Idea, BSNL, MTNL, ACT Fibernet, Atria Convergence, Tata Play, Tata Sky, Hathway, You Broadband, BESCOM, MSEDCL, Tata Power, Adani Electricity, BSES, TANGEDCO, TNEB, Torrent Power, CESC |
| Other eligible | Myntra, Amazon, Flipkart |
| Excluded | petrol, fuel, insurance, LIC of India, Life Insurance Corporation, HDFC Life, SBI Life, ICICI Prudential, Max Life, Bajaj Allianz, ICICI Lombard, Star Health, rent payment, wallet load, Indian Oil, IndianOil, Bharat Petroleum, Hindustan Petroleum, BPCL, HPCL |

The catalog is maintained in [Engine.java](../app/src/main/java/in/local/liveplus/Engine.java). See [reward assumptions](REWARDS.md) for rates, exclusions and estimation limits.
