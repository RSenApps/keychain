package keychain.com.keychain;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Sign;
import org.web3j.crypto.WalletUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.android.volley.VolleyLog.TAG;

/**
 * Created by rsen on 1/27/18.
 */


public class QRScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    public static final int PERMISSION_REQUEST_CAMERA = 1;
    private String callbackURL;
    private String nonce;

    private Credentials credentials;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);    // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

        // Request permission. This does it asynchronously so we have to wait for onRequestPermissionResult before trying to open the camera.
        if (!haveCameraPermission())
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        try {
            credentials = WalletUtils.loadCredentials(prefs.getString("password", ""), prefs.getString("walletPath", ""));
        } catch (Exception e) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        // This is because the dialog was cancelled when we recreated the activity.
        if (permissions.length == 0 || grantResults.length == 0)
            return;

        switch (requestCode)
        {
            case PERMISSION_REQUEST_CAMERA:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    startCamera();
                }
                else
                {
                    finish();
                }
            }
            break;
        }
    }

    public void startCamera()
    {
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }


    private boolean haveCameraPermission()
    {
        if (Build.VERSION.SDK_INT < 23)
            return true;
        return QRScannerActivity.this.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(TAG, rawResult.getText()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        try {
            if (!rawResult.getText().split(",")[0].equals("keychain")) {
                throw new InputMismatchException();
            }
            callbackURL = rawResult.getText().split(",")[1];
            nonce = rawResult.getText().split(",")[2];
            signNonce(nonce);
        }
        catch (Exception e) {
            Toast.makeText(this, "QR Code is not well formatted. Make sure it was created by KeyChain", Toast.LENGTH_LONG).show();
            mScannerView.resumeCameraPreview(this);
        }
        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }

    private void signNonce(String message) {
        try {
            Sign.SignatureData sig = Sign.signMessage((message + getSharedPreferences("prefs", MODE_PRIVATE).getString("keychainid", "")).getBytes(), credentials.getEcKeyPair());
            sendResponse(sig);
        }
        catch (Exception e) {
            e.printStackTrace();
            mScannerView.resumeCameraPreview(this);
        }
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    private void sendResponse(final Sign.SignatureData signedNonce) {
        try {
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            final String keychainid = prefs.getString("keychainid", "");
            //final String publicKeyX = bytesToHex(point.getAffineXCoord().toBigInteger().toByteArray());
            //final String publicKeyY = bytesToHex(point.getAffineYCoord().toBigInteger().toByteArray());

            StringRequest stringRequest = new StringRequest(Request.Method.POST, callbackURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(QRScannerActivity.this, response, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mScannerView.resumeCameraPreview(QRScannerActivity.this);
                    Toast.makeText(QRScannerActivity.this, "Request to Server failed. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
            ){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("v", String.valueOf(signedNonce.getV()));
                    params.put("r", bytesToHex(signedNonce.getR()));
                    params.put("s", bytesToHex(signedNonce.getS()));
                    params.put("nonce", nonce);
                    params.put("keychain_id",keychainid);
                    params.put("public_key", bytesToHex(credentials.getEcKeyPair().getPublicKey().toByteArray()));
                    params.put("address", credentials.getAddress());
                    Log.d("fefefe", String.valueOf(signedNonce.getV()));
                    Log.d("fefefe", bytesToHex(signedNonce.getR()));
                    Log.d("fefefe", bytesToHex(signedNonce.getS()));

                    Log.d("fefefe", nonce);
                    Log.d("fefefe", keychainid);
                    Log.d("fefefe", credentials.getAddress());
                    Log.d("fefefe", bytesToHex(credentials.getEcKeyPair().getPublicKey().toByteArray()));

                    return params;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(QRScannerActivity.this);
            queue.add(stringRequest);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // The user authenticated successfully, so let's try to en-/decrypt again.
            if (requestCode == 1) {
                signNonce(nonce);
            }
        } else {
            // The user canceled or didn’t complete the lock screen
            // operation. Go to error/cancellation flow.
        }

    }
}