import com.android.apksig.ApkVerifier;

import java.io.File;

/** Sanity-checks a built APK's signature; used by verify.sh. */
public final class Verify {
    public static void main(String[] args) throws Exception {
        ApkVerifier.Result r = new ApkVerifier.Builder(new File(args[0])).build().verify();
        System.out.println("verified   : " + r.isVerified());
        System.out.println("v2 scheme  : " + r.isVerifiedUsingV2Scheme());
        System.out.println("signers    : " + r.getSignerCertificates().size());
        for (ApkVerifier.IssueWithParams e : r.getErrors()) System.out.println("ERROR " + e);
        for (ApkVerifier.IssueWithParams w : r.getWarnings()) System.out.println("WARN  " + w);
        if (!r.isVerified()) System.exit(1);
    }
}
