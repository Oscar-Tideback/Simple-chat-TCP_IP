package Crypto;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.interfaces.Box;
import com.goterl.lazysodium.interfaces.KeyDerivation;
import com.goterl.lazysodium.interfaces.PwHash;
import com.goterl.lazysodium.interfaces.SecretBox;
import com.goterl.lazysodium.utils.Key;
import com.goterl.lazysodium.utils.KeyPair;
import com.goterl.lazysodium.utils.SessionPair;

import java.nio.charset.StandardCharsets;
import java.time.Duration;


public class CryptoTest {
    public static void main(String[] args) throws SodiumException {
        LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());

        secretKey(lazySodium);
        publicKey(lazySodium);
        sign(lazySodium);
        sealedBox(lazySodium);
        hash(lazySodium);
        passwordHash(lazySodium);
        keyDerivation(lazySodium);
        keyExchange(lazySodium);
        //lazySodium.cryptoPwHashStr("A cool password", PwHash.PWHASH_ARGON2ID_OPSLIMIT_MIN, PwHash.PWHASH_ARGON2ID_MEMLIMIT_MIN);
    }

    private static void keyExchange(LazySodiumJava lazySodium) throws SodiumException {
        KeyPair client = lazySodium.cryptoKxKeypair();
        KeyPair server = lazySodium.cryptoKxKeypair();

        SessionPair clientKey = lazySodium.cryptoKxClientSessionKeys(client.getPublicKey(), client.getSecretKey(), server.getPublicKey());
        System.out.println(clientKey.getRxString());
        System.out.println(clientKey.getTxString());

        SessionPair serverKey = lazySodium.cryptoKxServerSessionKeys(server.getPublicKey(), server.getSecretKey(), client.getPublicKey());
        System.out.println(serverKey.getRxString());
        System.out.println(serverKey.getTxString());

        // Send something from client to server
        byte[] nonce = lazySodium.nonce(SecretBox.NONCEBYTES);
        String ciphertext = lazySodium.cryptoSecretBoxEasy("Client -> Server", nonce, Key.fromBytes(clientKey.getTx()));
        System.out.println(ciphertext);
        String plaintext = lazySodium.cryptoSecretBoxOpenEasy(ciphertext, nonce, Key.fromBytes(serverKey.getRx()));
        System.out.println(plaintext);

        // Send something from server to client
        byte[] nonce2 = lazySodium.nonce(SecretBox.NONCEBYTES);
        String ciphertext2 = lazySodium.cryptoSecretBoxEasy("Server -> Client", nonce2, Key.fromBytes(serverKey.getTx()));
        System.out.println(ciphertext2);
        String plaintext2 = lazySodium.cryptoSecretBoxOpenEasy(ciphertext2, nonce2, Key.fromBytes(clientKey.getRx()));
        System.out.println(plaintext2);
    }
    private static void keyDerivation(LazySodiumJava lazySodium) throws SodiumException {
        Key masterKey = lazySodium.cryptoKdfKeygen();
        System.out.println(masterKey.getAsHexString());

        for (int i = 0; i < 10; i++) {
            // Context must be 8 bytes long, see https://download.libsodium.org/doc/key_derivation
            String context = "context!";
            Key key = lazySodium.cryptoKdfDeriveFromKey(KeyDerivation.MASTER_KEY_BYTES, i, context, masterKey);
            System.out.println(key.getAsHexString());
        }
    }
    private static void passwordHash(LazySodiumJava lazySodium) throws SodiumException {
        long start = System.nanoTime();
        String hash = lazySodium.cryptoPwHashStr("hello world", PwHash.OPSLIMIT_INTERACTIVE, PwHash.MEMLIMIT_INTERACTIVE);
        long end = System.nanoTime();
        System.out.println(hash);
        System.out.printf("Hash took %d ms%n", Duration.ofNanos(end - start).toMillis());

        boolean matches = lazySodium.cryptoPwHashStrVerify(hash, "hello world");
        System.out.println(matches);
    }
    private static void hash(LazySodiumJava lazySodium) throws SodiumException {
        String hash = lazySodium.cryptoGenericHash("hello world");
        System.out.println(hash);

        Key key = lazySodium.cryptoGenericHashKeygen();
        String mac = lazySodium.cryptoGenericHash("hello world", key);
        System.out.println(mac);
    }

    private static void sealedBox(LazySodiumJava lazySodium) throws SodiumException {
        byte[] message = "Hello world".getBytes(StandardCharsets.UTF_8);
        byte[] cipher = new byte[Box.SEALBYTES + message.length];

        KeyPair alice = lazySodium.cryptoBoxKeypair();

        boolean success = lazySodium.cryptoBoxSeal(cipher, message, message.length, alice.getPublicKey().getAsBytes());
        if (!success) {
            throw new SodiumException("cryptoBoxSeal failed");
        }
        System.out.println(lazySodium.sodiumBin2Hex(cipher));

        byte[] plaintext = new byte[message.length];
        success = lazySodium.cryptoBoxSealOpen(plaintext, cipher, cipher.length, alice.getPublicKey().getAsBytes(), alice.getSecretKey().getAsBytes());
        if (!success) {
            throw new SodiumException("cryptoBoxSealOpen failed");
        }

        System.out.println(new String(plaintext, StandardCharsets.UTF_8));
    }

    private static void sign(LazySodiumJava lazySodium) throws SodiumException {
        KeyPair alice = lazySodium.cryptoSignKeypair();

        String signature = lazySodium.cryptoSign("hello world", alice.getSecretKey().getAsHexString());
        System.out.println(signature);
        String payload = lazySodium.cryptoSignOpen(signature, alice.getPublicKey());
        System.out.println(payload);
    }

    private static void publicKey(LazySodiumJava lazySodium) throws SodiumException {
        KeyPair alice = lazySodium.cryptoBoxKeypair();
        KeyPair bob = lazySodium.cryptoBoxKeypair();

        byte[] nonce = lazySodium.nonce(Box.NONCEBYTES);
        String ciphertext = lazySodium.cryptoBoxEasy("Hello World", nonce, new KeyPair(bob.getPublicKey(), alice.getSecretKey()));
        System.out.println(ciphertext);

        String plaintext = lazySodium.cryptoBoxOpenEasy(ciphertext, nonce, new KeyPair(alice.getPublicKey(), bob.getSecretKey()));
        System.out.println(plaintext);
    }

    private static void secretKey(LazySodiumJava lazySodium) throws SodiumException {
        Key key = lazySodium.cryptoSecretBoxKeygen();

        byte[] nonce = lazySodium.nonce(SecretBox.NONCEBYTES);
        String ciphertext = lazySodium.cryptoSecretBoxEasy("Hello World", nonce, key);
        System.out.println(ciphertext);

        String plaintext = lazySodium.cryptoSecretBoxOpenEasy(ciphertext, nonce, key);
        System.out.println(plaintext);

        String mac = lazySodium.cryptoAuth("hello world", key);
        System.out.println(mac);

        boolean verified = lazySodium.cryptoAuthVerify(mac, "hello world", key);
        System.out.println(verified);
    }
    // Developer calls a Lazysodium "lazy" function
     // lazySodium.cryptoPwHashStr("A cool password", PwHash.PWHASH_ARGON2ID_OPSLIMIT_MIN, PwHash.PWHASH_ARGON2ID_MEMLIMIT_MIN);

    /*private String getLibSodiumFromResources() {
        String path = getPath("windows", "libsodium.dll");
        if (Platform.isLinux() || Platform.isAndroid()) {
            path = getPath("linux", "libsodium.so");
        } else if (Platform.isMac()) {
            path = getPath("mac", "libsodium.dylib");
        }
        return path;
    }*/
}
