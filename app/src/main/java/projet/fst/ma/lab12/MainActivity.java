package projet.fst.ma.lab12;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityGPS";
    private static final int REQ_LOC = 100;

    private TextView tvLat, tvLon;
    private RequestQueue requestQueue;
    private LocationManager locationManager;
    private double lastLat = 31.6295; // Marrakech par défaut
    private double lastLon = -7.9811;

    // URL vers le dossier 'localisation2'
    // ATTENTION : Vérifiez que l'IP 192.168.43.228 est bien celle de votre PC (ipconfig)
    private final String insertUrl = "http://192.168.43.228/localisation2/createPosition.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLat = findViewById(R.id.tvLat);
        tvLon = findViewById(R.id.tvLon);
        Button btnMap = findViewById(R.id.btnMap);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("lat", lastLat);
            intent.putExtra("lon", lastLon);
            startActivity(intent);
        });

        askLocationPermissionAndStart();
    }

    private void askLocationPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOC);
        } else {
            startGpsUpdates();
        }
    }

    @SuppressLint("MissingPermission")
    private void startGpsUpdates() {
        // Paramètres de test : 5 secondes et 0 mètre pour voir les mises à jour immédiatement
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000, 
                0,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        lastLat = location.getLatitude();
                        lastLon = location.getLongitude();
                        double alt = location.getAltitude();
                        float acc = location.getAccuracy();

                        tvLat.setText("Latitude: " + lastLat);
                        tvLon.setText("Longitude: " + lastLon);

                        Log.d(TAG, "Position obtenue : " + lastLat + ", " + lastLon);

                        addPosition(lastLat, lastLon);

                        String msg = String.format(
                                getResources().getString(R.string.new_location),
                                String.valueOf(lastLat), String.valueOf(lastLon), String.valueOf(alt), String.valueOf(acc)
                        );
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}

                    @Override
                    public void onProviderEnabled(@NonNull String provider) {
                        Log.d(TAG, "Provider activé : " + provider);
                    }

                    @Override
                    public void onProviderDisabled(@NonNull String provider) {
                        Log.d(TAG, "Provider désactivé : " + provider);
                        Toast.makeText(MainActivity.this, "Activez le GPS !", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void addPosition(final double lat, final double lon) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                insertUrl,
                response -> {
                    Log.d(TAG, "Réponse serveur PHP : " + response);
                    Toast.makeText(getApplicationContext(), "Serveur : " + response, Toast.LENGTH_SHORT).show();
                },
                (VolleyError error) -> {
                    String errorMsg = "Erreur Volley : " + error.toString();
                    if (error.networkResponse != null) {
                        errorMsg += " Code: " + error.networkResponse.statusCode;
                    }
                    Log.e(TAG, errorMsg);
                    Toast.makeText(getApplicationContext(), "Timeout ! Vérifiez votre IP/Firewall PC.", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                
                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));
                params.put("date", sdf.format(new Date()));
                params.put("imei", getDeviceIdentifier());
                
                Log.d(TAG, "Envoi au serveur : " + params.toString());
                return params;
            }
        };
        
        // Augmenter le timeout à 10 secondes pour éviter le TimeoutError
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    private String getDeviceIdentifier() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId != null && !androidId.trim().isEmpty()) return androidId;
        return "ID_" + android.os.Build.ID;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGpsUpdates();
        } else {
            Toast.makeText(this, "Permission GPS refusée", Toast.LENGTH_LONG).show();
        }
    }
}
