import com.android.apksig.ApkVerifier;

import java.io.File;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;

/** Sanity-checks a built APK's signature; used by verify.sh. */
public final class Verify {
    public static void main(String[] args) throws Exception {
        ApkVerifier.Result r = new ApkVerifier.Builder(new File(args[0])).build().verify();
        System.out.println("verified   : " + r.isVerified());
        System.out.println("v2 scheme  : " + r.isVerifiedUsingV2Scheme());
        System.out.println("signers    : " + r.getSignerCertificates().size());
        // Two APKs only update each other if this matches.
        for (X509Certificate cert : r.getSignerCertificates()) {
            System.out.println("signer sha256: " + fingerprint(cert));
        }
        for (ApkVerifier.IssueWithParams e : r.getErrors()) System.out.println("ERROR " + e);
        for (ApkVerifier.IssueWithParams w : r.getWarnings()) System.out.println("WARN  " + w);
        if (!r.isVerified()) System.exit(1);
    }

    private static String fingerprint(X509Certificate cert) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(cert.getEncoded());
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            if (i > 0) out.append(':');
            out.append(String.format("%02X", digest[i] & 0xFF));
        }
        return out.toString();
    }
}
