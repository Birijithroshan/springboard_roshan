package org.automation.ui;

import java.util.HashMap;
import java.util.Map;

public class UiTestMapper {

    public static final String HOME_URL = "https://blazedemo.com";
    public static final String FLIGHTS_URL = "https://blazedemo.com/reserve.php";
    public static final String PURCHASE_URL = "https://blazedemo.com/purchase.php";
    public static final String CONFIRMATION_URL = "https://blazedemo.com/confirmation.php";

    public static Map<String, String> uiTests() {
        Map<String, String> map = new HashMap<>();
        map.put("testHomePageTitle", "US101");
        map.put("testFlightSearchNavigation", "US102");
        map.put("testSelectFirstFlight", "US103");
        map.put("testPurchaseFlight", "US104");
        map.put("testFillPurchaseForm", "US105");
        map.put("testPurchasePageContainsForm", "US106");
        map.put("testConfirmationPage", "US107");
        map.put("testHomePageContainsDepartureAndDestination", "US108");
        map.put("testFlightSelectionPage", "US109");
        map.put("testPurchaseFlightVerification", "US110");
        return map;
    }
}
