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
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);    // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

        // Request permission. This does it asynchronously so we have to wait for onRequestPermissionResult before trying to open the camera.
        if (!haveCameraPermission())
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);

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
            BigInteger[] output = Cryptography.sign(message + Long.toHexString(getSharedPreferences("prefs", MODE_PRIVATE).getLong("keychain-id", -1)));
            sendResponse(output);
        } catch (UserNotAuthenticatedException e) {
            Intent in = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).createConfirmDeviceCredentialIntent(
                    "KeyChain", "Please log in to confirm access to your account.");
            startActivityForResult(in, 1);
        } catch (Exception e) {
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

    private void sendResponse(final BigInteger[] signedNonce) {
        try {
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            final long keychainid = prefs.getLong("keychain-id", -1);
            final String publicKey = Cryptography.getPublickey();
            final String publicKeyX = bytesToHex(Cryptography.getPublicPoint().getAffineX().toByteArray());
            final String publicKeyY = bytesToHex(Cryptography.getPublicPoint().getAffineY().toByteArray());

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
                    Log.d(TAG, error.getMessage());
                }
            }
            ){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("signatureR", bytesToHex(signedNonce[0].toByteArray()));
                    params.put("signatureS", bytesToHex(signedNonce[1].toByteArray()));
                    params.put("nonce", nonce);
                    params.put("keychain_id", Long.toHexString(keychainid));
                    params.put("public_keyx", publicKeyX);
                    params.put("public_keyy", publicKeyY);
                    Log.d("fefefe", bytesToHex(signedNonce[0].toByteArray()));
                    Log.d("fefefe", bytesToHex(signedNonce[1].toByteArray()));
                    Log.d("fefefe", nonce);
                    Log.d("fefefe", publicKey);
                    Log.d("fefefe", publicKeyX);
                    Log.d("fefefe", publicKeyY);

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