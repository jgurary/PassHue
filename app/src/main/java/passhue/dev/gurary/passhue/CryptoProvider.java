package passhue.dev.gurary.passhue;

/**
 * Created by Commander Fish on 8/11/2017.
 */

import java.security.Provider;
/**
 * This is a potentially stupid workaround for Android blocking the Crypto algorithm
 * It fetches the source from the internet instead, forcing Android to use it.
 */
public final class CryptoProvider extends Provider {

    public CryptoProvider() {
        super("Crypto", 1.0, "HARMONY (SHA1 digest; SecureRandom; SHA1withDSA signature)");
        put("SecureRandom.SHA1PRNG",
                "org.apache.harmony.security.provider.crypto.SHA1PRNG_SecureRandomImpl");
        put("SecureRandom.SHA1PRNG ImplementedIn", "Software");
    }
}