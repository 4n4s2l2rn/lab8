package com.example.labthreadsasynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // 1) Références vers l'interface
    private TextView txtStatus;
    private ProgressBar progressBar;
    private ImageView img;

    // 2) Handler lié au UI thread (Main thread)
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // A) Lier les vues XML au code Java (findViewById)
        txtStatus   = findViewById(R.id.txtStatus);
        progressBar = findViewById(R.id.progressBar);
        img         = findViewById(R.id.img);

        Button btnLoadThread = findViewById(R.id.btnLoadThread);
        Button btnCalcAsync  = findViewById(R.id.btnCalcAsync);
        Button btnToast      = findViewById(R.id.btnToast);

        // B) Créer le Handler qui poste sur le UI thread
        mainHandler = new Handler(Looper.getMainLooper());

        // C) Bouton Toast : doit toujours répondre immédiatement
        btnToast.setOnClickListener(v ->
                Toast.makeText(getApplicationContext(), "UI réactive !", Toast.LENGTH_SHORT).show()
        );

        // D) Lancer un Thread pour charger une image sans bloquer l'UI
        btnLoadThread.setOnClickListener(v -> loadImageWithThread());

        // E) Lancer un calcul lourd avec AsyncTask
        btnCalcAsync.setOnClickListener(v -> new HeavyCalcTask().execute());
    }

    // -----------------------------------------
    // PARTIE 1 : THREAD
    // -----------------------------------------
    private void loadImageWithThread() {

        // 1) Afficher la ProgressBar sur le UI thread
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        txtStatus.setText("Statut : chargement image (Thread)...");

        // 2) Créer et démarrer un thread de fond
        new Thread(() -> {

            // 3) Simuler un téléchargement (travail long)
            try {
                Thread.sleep(1000); // 1 seconde
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 4) Charger l'image en arrière-plan
            // On utilise l'icône du launcher déjà présente dans le projet
            Bitmap bitmap = BitmapFactory.decodeResource(
                    getResources(),
                    R.mipmap.ic_launcher
            );

            // 5) Revenir au UI thread via Handler pour mettre à jour l'interface
            //    IMPORTANT : on ne peut JAMAIS modifier l'UI depuis un thread de fond !
            mainHandler.post(() -> {
                img.setImageBitmap(bitmap);
                progressBar.setVisibility(View.INVISIBLE);
                txtStatus.setText("Statut : image chargée ✓ (Thread)");
            });

        }).start(); // 6) start() démarre réellement le thread
    }

    // -----------------------------------------
    // PARTIE 2 : ASYNCTASK
    // -----------------------------------------
    private class HeavyCalcTask extends AsyncTask<Void, Integer, Long> {

        // Étape 1 — Avant le traitement : s'exécute sur le UI thread
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            txtStatus.setText("Statut : calcul lourd (AsyncTask)...");
        }

        // Étape 2 — Traitement long : s'exécute sur un Worker thread (pas l'UI)
        @Override
        protected Long doInBackground(Void... voids) {
            long result = 0;

            for (int i = 1; i <= 100; i++) {

                // Simulation d'un calcul intensif
                for (int k = 0; k < 200_000; k++) {
                    result += (i * k) % 7;
                }

                // Envoie la progression au UI thread → déclenche onProgressUpdate()
                publishProgress(i);
            }

            return result; // Valeur transmise à onPostExecute()
        }

        // Étape 3 — Mise à jour de la progression : s'exécute sur le UI thread
        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }

        // Étape 4 — Après le traitement : s'exécute sur le UI thread
        @Override
        protected void onPostExecute(Long result) {
            progressBar.setVisibility(View.INVISIBLE);
            txtStatus.setText("Statut : calcul terminé ✓  résultat = " + result);
        }
    }
}