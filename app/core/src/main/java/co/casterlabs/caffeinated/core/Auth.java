package co.casterlabs.caffeinated.core;

public class Auth {

    public void init() {
        for (String tokenId : App.INSTANCE.preferences.auth.getAllTokenIdsByType("koi")) {
            App.INSTANCE.koi.connect(tokenId);
        }
    }

}
