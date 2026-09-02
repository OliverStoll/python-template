import com.android.apksig.ApkSigner;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Minimal stand-in for the apksigner CLI, built on the apksig library. */
public final class Signer {

    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            System.err.println("usage: Signer <in.apk> <out.apk> <keystore> <storepass> <alias> <minSdk>");
            System.exit(2);
        }
        File in = new File(args[0]);
        File out = new File(args[1]);
        String storePass = args[3];
        String alias = args[4];
        int minSdk = Integer.parseInt(args[5]);

        KeyStore ks = KeyStore.getInstance("PKCS12");
        FileInputStream fis = new FileInputStream(args[2]);
        try {
            ks.load(fis, storePass.toCharArray());
        } finally {
            fis.close();
        }

        PrivateKey key = (PrivateKey) ks.getKey(alias, storePass.toCharArray());
        java.security.cert.Certificate[] chain = ks.getCertificateChain(alias);
        List<X509Certificate> certs = new ArrayList<X509Certificate>();
        for (java.security.cert.Certificate c : chain) certs.add((X509Certificate) c);

        ApkSigner.SignerConfig config =
                new ApkSigner.SignerConfig.Builder("CERT", key, certs).build();

        new ApkSigner.Builder(Collections.singletonList(config))
                .setInputApk(in)
                .setOutputApk(out)
                .setMinSdkVersion(minSdk)
                // v1 (jar) signing is deliberately off: apksig 2.3.0 drives it
                // through sun.security.pkcs internals that JDK 9+ removed, and
                // with minSdk 24 every target device already verifies v2.
                .setV1SigningEnabled(false)
                .setV2SigningEnabled(true)
                .build()
                .sign();

        System.out.println("signed -> " + out.getPath());
    }
}
