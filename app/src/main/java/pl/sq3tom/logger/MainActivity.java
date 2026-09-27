package pl.sq3tom.logger;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout terrain, history, acts, satBox;

    EditText call, dt, freq, rstS, rstR, power, grid, comment;
    EditText satRx, satTx, actRef, actQth;

    TextView status, person, gpsInfo, actInfo;

    Spinner band, mode, satellite, satType;

    String activation = "";

    ArrayList<String> qsos = new ArrayList<>();

    SharedPreferences pref;

    String[] bands = {
            "2m", "70cm", "10m", "12m", "15m",
            "17m", "20m", "30m", "40m", "80m", "160m"
    };

    String[] modes = {
            "AM", "SSB", "DMR", "FM", "C4FM",
            "FT8", "FT4", "JS8", "CW"
    };

    String[] satellites = {
            "ISS", "SO-50", "AO-91", "AO-92",
            "PO-101", "RS-44", "Inny"
    };

    String[] satelliteTypes = {
            "FM SPLIT", "SSB SPLIT", "APRS", "Packet"
    };


    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("logger", Context.MODE_PRIVATE);

        bind();
        setup();
    }


    void bind() {

        terrain = findViewById(R.id.terrain);
        history = findViewById(R.id.history);
        acts = findViewById(R.id.activations);
        satBox = findViewById(R.id.satBox);

        call = findViewById(R.id.call);
        dt = findViewById(R.id.datetime);
        freq = findViewById(R.id.freq);
        rstS = findViewById(R.id.rstSent);
        rstR = findViewById(R.id.rstRcvd);
        power = findViewById(R.id.power);
        grid = findViewById(R.id.grid);
        comment = findViewById(R.id.comment);

        satRx = findViewById(R.id.satRx);
        satTx = findViewById(R.id.satTx);

        actRef = findViewById(R.id.actRef);
        actQth = findViewById(R.id.actQth);

        status = findViewById(R.id.status);
        person = findViewById(R.id.person);
        gpsInfo = findViewById(R.id.gpsInfo);
        actInfo = findViewById(R.id.actInfo);

        band = findViewById(R.id.band);
        mode = findViewById(R.id.mode);
        satellite = findViewById(R.id.satellite);
        satType = findViewById(R.id.satType);
    }


    void setup() {

        band.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        bands
                )
        );

        mode.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        modes
                )
        );

        satellite.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        satellites
                )
        );

        satType.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        satelliteTypes
                )
        );


        dt.setText(utc());

        load();


        // TEREN
        findViewById(R.id.tabTerrain).setOnClickListener(
                v -> show(terrain)
        );


        // HISTORIA
        findViewById(R.id.tabHistory).setOnClickListener(
                v -> {
                    renderHistory();
                    show(history);
                }
        );


        // AKTYWACJE
        findViewById(R.id.tabAct).setOnClickListener(
                v -> show(acts)
        );


        // ZAPISZ QSO
        findViewById(R.id.save).setOnClickListener(
                v -> saveQso()
        );


        // NOWE QSO
        findViewById(R.id.newQso).setOnClickListener(
                v -> clear()
        );


        // GPS
        findViewById(R.id.gps).setOnClickListener(
                v -> gps()
        );


        // SAT / SPLIT
        findViewById(R.id.sat).setOnClickListener(
                v -> toggleSat()
        );


        // QRZ
        findViewById(R.id.lookup).setOnClickListener(
                v -> lookup()
        );


        // AKTYWACJE
        View.OnClickListener activationListener = v -> {

            activation =
                    ((Button) v)
                            .getText()
                            .toString()
                            .replaceAll("[^A-Z]", "");

            actInfo.setText(
                    "Wybrano: " + activation
            );
        };


        findViewById(R.id.pota).setOnClickListener(
                activationListener
        );

        findViewById(R.id.sota).setOnClickListener(
                activationListener
        );

        findViewById(R.id.llota).setOnClickListener(
                activationListener
        );

        findViewById(R.id.wwff).setOnClickListener(
                activationListener
        );


        // ZAPIS AKTYWACJI
        findViewById(R.id.saveAct).setOnClickListener(
                v -> {

                    pref.edit()
                            .putString("act", activation)
                            .apply();

                    actInfo.setText(
                            "Aktywna: "
                                    + activation
                                    + " "
                                    + actRef.getText()
                                    + " "
                                    + actQth.getText()
                    );

                    toast("Aktywacja zapisana");
                }
        );


        // WYBÓR SATELITY
        satellite.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        presetSat(
                                parent
                                        .getItemAtPosition(position)
                                        .toString()
                        );
                    }
                }
        );
    }


    // --------------------------------------------------
    // PRZEŁĄCZANIE PANELI
    // --------------------------------------------------

    void show(View selected) {

        terrain.setVisibility(
                selected == terrain
                        ? View.VISIBLE
                        : View.GONE
        );

        history.setVisibility(
                selected == history
                        ? View.VISIBLE
                        : View.GONE
        );

        acts.setVisibility(
                selected == acts
                        ? View.VISIBLE
                        : View.GONE
        );
    }


    // --------------------------------------------------
    // SAT / SPLIT
    // --------------------------------------------------

    void toggleSat() {

        if (satBox.getVisibility() == View.VISIBLE) {

            satBox.setVisibility(View.GONE);

            toast("SAT / SPLIT wyłączony");

        } else {

            satBox.setVisibility(View.VISIBLE);

            toast("SAT / SPLIT włączony");
        }
    }


    // --------------------------------------------------
    // CZAS UTC
    // --------------------------------------------------

    String utc() {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm 'UTC'",
                        Locale.US
                );

        format.setTimeZone(
                TimeZone.getTimeZone("UTC")
        );

        return format.format(new Date());
    }


    // --------------------------------------------------
    // ZAPIS QSO
    // --------------------------------------------------

    void saveQso() {

        String c =
                call.getText()
                        .toString()
                        .trim()
                        .toUpperCase();

        if (c.isEmpty()) {

            toast("Wpisz znak korespondenta");

            call.requestFocus();

            return;
        }


        String q =
                c
                + " | "
                + dt.getText()
                + " | "
                + band.getSelectedItem()
                + " | "
                + mode.getSelectedItem()
                + " | "
                + freq.getText()
                + " MHz"
                + " | RST "
                + rstS.getText()
                + "/"
                + rstR.getText()
                + " | "
                + power.getText()
                + " W";


        if (!activation.isEmpty()) {

            q +=
                    " | "
                    + activation
                    + " "
                    + actRef.getText()
                    + " "
                    + actQth.getText();
        }


        qsos.add(q);


        saveQsoList();


        status.setText(
                "● zapisano: " + c
        );


        toast("QSO zapisane");


        clear();
    }


    // --------------------------------------------------
    // ZAPIS LISTY QSO
    // --------------------------------------------------

    void saveQsoList() {

        LinkedHashSet<String> set =
                new LinkedHashSet<>(qsos);

        pref.edit()
                .putStringSet("qsos", set)
                .putString(
                        "band",
                        band.getSelectedItem().toString()
                )
                .putString(
                        "mode",
                        mode.getSelectedItem().toString()
                )
                .putString(
                        "freq",
                        freq.getText().toString()
                )
                .putString(
                        "power",
                        power.getText().toString()
                )
                .apply();
    }


    // --------------------------------------------------
    // NOWE QSO
    // --------------------------------------------------

    void clear() {

        call.setText("");

        comment.setText("");

        rstS.setText("59");

        rstR.setText("59");

        dt.setText(utc());

        status.setText("● gotowy");

        call.requestFocus();
    }


    // --------------------------------------------------
    // HISTORIA
    // --------------------------------------------------

    void renderHistory() {

        history.removeAllViews();


        if (qsos.isEmpty()) {

            TextView empty =
                    new TextView(this);

            empty.setText(
                    "Brak zapisanych QSO."
            );

            empty.setTextColor(
                    Color.WHITE
            );

            empty.setTextSize(18);

            empty.setPadding(
                    20,
                    20,
                    20,
                    20
            );

            history.addView(empty);

            return;
        }


        for (String q : qsos) {

            Button button =
                    new Button(this);

            button.setText(q);

            button.setTextColor(
                    Color.WHITE
            );


            button.setOnClickListener(
                    v -> {

                        String[] parts =
                                q.split(" \\|");

                        if (parts.length > 0) {

                            call.setText(
                                    parts[0].trim()
                            );
                        }


                        show(terrain);

                        toast(
                                "QSO wybrane"
                        );
                    }
            );


            history.addView(button);
        }
    }


    // --------------------------------------------------
    // WCZYTANIE DANYCH
    // --------------------------------------------------

    void load() {

        qsos.clear();


        Set<String> saved =
                pref.getStringSet(
                        "qsos",
                        new LinkedHashSet<>()
                );


        if (saved != null) {

            qsos.addAll(saved);
        }


        freq.setText(
                pref.getString(
                        "freq",
                        ""
                )
        );


        power.setText(
                pref.getString(
                        "power",
                        "5"
                )
        );


        activation =
                pref.getString(
                        "act",
                        ""
                );


        if (activation.isEmpty()) {

            actInfo.setText(
                    "Brak aktywnej aktywacji."
            );

        } else {

            actInfo.setText(
                    "Aktywna: "
                            + activation
            );
        }
    }


    // --------------------------------------------------
    // QRZ
    // --------------------------------------------------

    void lookup() {

        String c =
                call.getText()
                        .toString()
                        .trim()
                        .toUpperCase();


        if (c.isEmpty()) {

            toast(
                    "Najpierw wpisz znak"
            );

            return;
        }


        person.setText(
                c
                        + "\nWyszukiwanie QRZ..."
        );

        person.setVisibility(
                View.VISIBLE
        );


        toast(
                "Moduł QRZ będzie podłączony w kolejnym etapie"
        );
    }


    // --------------------------------------------------
    // SATELITY
    // --------------------------------------------------

    void presetSat(String s) {

        String rx = "";
        String tx = "";


        if (s.equals("ISS")) {

            rx = "145.800";
            tx = "145.200";

        } else if (s.equals("SO-50")) {

            rx = "436.800";
            tx = "145.850";

        } else if (s.equals("AO-91")) {

            rx = "145.960";
            tx = "435.250";

        } else if (s.equals("AO-92")) {

            rx = "145.880";
            tx = "437.350";

        } else if (s.equals("PO-101")) {

            rx = "437.500";
            tx = "145.900";

        } else if (s.equals("RS-44")) {

            rx = "145.965";
            tx = "145.935";
        }


        satRx.setText(rx);

        satTx.setText(tx);
    }


    // --------------------------------------------------
    // GPS
    // --------------------------------------------------

    void gps() {

        if (
                android.os.Build.VERSION.SDK_INT >= 23
                        &&
                checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    9
            );

            return;
        }


        LocationManager lm =
                (LocationManager)
                        getSystemService(
                                LOCATION_SERVICE
                        );


        try {

            Location location =
                    lm.getLastKnownLocation(
                            LocationManager.GPS_PROVIDER
                    );


            if (location == null) {

                location =
                        lm.getLastKnownLocation(
                                LocationManager.NETWORK_PROVIDER
                        );
            }


            if (location != null) {

                grid.setText(
                        locator(
                                location.getLatitude(),
                                location.getLongitude()
                        )
                );


                gpsInfo.setText(
                        "📍 "
                                + location.getLatitude()
                                + ", "
                                + location.getLongitude()
                                + " · "
                                + grid.getText()
                );


                toast(
                        "Pozycja GPS odczytana"
                );

            } else {

                toast(
                        "Brak pozycji GPS — spróbuj ponownie"
                );
            }

        } catch (Exception e) {

            toast(
                    "GPS niedostępny"
            );
        }
    }


    // --------------------------------------------------
    // LOCATOR
    // --------------------------------------------------

    String locator(
            double lat,
            double lon
    ) {

        String letters =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ";


        lon += 180;

        lat += 90;


        int a =
                (int) (lon / 20);

        int b =
                (int) (lat / 10);

        int c =
                (int) ((lon % 20) / 2);

        int d =
                (int) (lat % 10);


        int e =
                (int) ((lon % 2) * 12);

        int f =
                (int) (((lat % 10) % 2) * 24);


        return ""
                + letters.charAt(a)
                + letters.charAt(b)
                + c
                + d
                + letters.charAt(e)
                + letters.charAt(f);
    }


    // --------------------------------------------------
    // KOMUNIKAT
    // --------------------------------------------------

    void toast(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}
