# Lab — Threads & AsyncTask Android

> **Niveau** : Débutant · **Durée estimée** : 1h30 · **Langage** : Java · **Min SDK** : API 21

---

## Objectifs pédagogiques

À la fin de ce lab, vous serez capable de :

- Comprendre pourquoi l'UI thread ne doit jamais être bloqué
- Créer un `Thread` de fond et y exécuter un travail long
- Utiliser un `Handler` pour reposter du code sur le UI thread
- Implémenter un `AsyncTask` avec ses 4 méthodes cycle de vie
- Valider qu'une interface reste réactive pendant un traitement en arrière-plan

---

## Structure du projet

```
LabThreadsAsyncTask/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/labthreadsasynctask/
│           │   └── MainActivity.java        ← Code principal du lab
│           └── res/
│               └── layout/
│                   └── activity_main.xml    ← Interface utilisateur
└── README.md
```

---

## Prérequis

| Outil | Version recommandée |
|---|---|
| Android Studio | Hedgehog (2023.1.1) ou supérieur |
| JDK | 17 |
| Android SDK | API 21 minimum, API 34 cible |
| Émulateur | Pixel 6 API 33 ou appareil réel |

---

## Installation & lancement

```bash
# 1. Cloner ou ouvrir le projet dans Android Studio
File → Open → sélectionner le dossier racine du projet

# 2. Laisser Gradle synchroniser (attendre la barre de progression en bas)

# 3. Lancer sur l'émulateur
Shift + F10   (Windows/Linux)
Ctrl + R      (macOS)
```

---

## Étapes du lab

### Étape 1 — Créer le projet

- `File → New → New Project → Empty Views Activity`
- **Name** : `LabThreadsAsyncTask`
- **Package** : `com.example.labthreadsasynctask`
- **Language** : Java
- **Minimum SDK** : API 21

### Étape 2 — Interface XML

Remplacer le contenu de `res/layout/activity_main.xml`.

L'interface contient :

| Vue | ID | Rôle |
|---|---|---|
| `TextView` | `txtStatus` | Affiche le statut en cours |
| `ProgressBar` | `progressBar` | Progression 0–100 |
| `ImageView` | `img` | Image chargée en arrière-plan |
| `Button` | `btnLoadThread` | Déclenche le Thread |
| `Button` | `btnCalcAsync` | Déclenche l'AsyncTask |
| `Button` | `btnToast` | Teste la réactivité de l'UI |

### Étape 3 — Code Java

Remplacer le contenu de `MainActivity.java`. Le fichier contient deux parties :

**Partie 1 — Thread**

```java
new Thread(() -> {
    Thread.sleep(1000);                          // travail long en arrière-plan
    Bitmap bitmap = BitmapFactory.decodeResource(...);

    mainHandler.post(() -> {                     // retour sur le UI thread
        img.setImageBitmap(bitmap);
    });
}).start();
```

**Partie 2 — AsyncTask**

```java
private class HeavyCalcTask extends AsyncTask<Void, Integer, Long> {
    onPreExecute()       // UI thread — prépare l'interface
    doInBackground()     // Worker thread — calcul lourd
    onProgressUpdate()   // UI thread — met à jour la ProgressBar
    onPostExecute()      // UI thread — affiche le résultat
}
```

### Étape 4 — Imports & Build

```
Ctrl + Shift + O   → optimiser les imports automatiquement
Build → Clean Project
Build → Rebuild Project
```

### Étape 5 — Tests de validation

| Action | Résultat attendu |
|---|---|
| Clic **Charger image (Thread)** | ProgressBar visible 1 s, puis image affichée |
| Clic **Afficher Toast** pendant le chargement | Toast immédiat → UI non bloquée ✓ |
| Clic **Calcul lourd (AsyncTask)** | ProgressBar monte de 0 à 100 progressivement |
| Clic **Afficher Toast** pendant le calcul | Toast immédiat → UI non bloquée ✓ |
| Fin du calcul | Statut affiche `calcul terminé ✓ résultat = …` |

---

## Concepts clés

### Pourquoi ne pas faire de travail long sur le UI thread ?

Android bloque l'application avec une erreur **ANR** (_Application Not Responding_)
si le UI thread est bloqué plus de **5 secondes**. Tout travail long doit donc
s'exécuter sur un thread de fond.

### Thread + Handler

```
Thread de fond  ──────────────────────────────────►  mainHandler.post()
                  travail long (sleep, réseau, I/O)       │
                                                           ▼
                                                     UI thread
                                                  (mise à jour vues)
```

### Cycle de vie AsyncTask

```
[UI thread]     onPreExecute()
                     │
[Worker thread] doInBackground()  ←──  publishProgress()
                     │                        │
[UI thread]          │               onProgressUpdate()
                     │
[UI thread]     onPostExecute()
```

> **Note** : `AsyncTask` est déprécié depuis **API 30**. Pour un projet en production,
> privilégier `Executors` + `Handler`, ou les **Kotlin Coroutines** (`viewModelScope.launch`).

---

## Dépannage

| Symptôme | Cause probable | Solution |
|---|---|---|
| `CalledFromWrongThreadException` | Modification de l'UI depuis un thread de fond | Entourer le code UI dans `mainHandler.post(...)` |
| ProgressBar reste visible après le calcul | `onPostExecute` incomplet | Vérifier `progressBar.setVisibility(View.INVISIBLE)` |
| Build échoue sur imports | Imports manquants | `Ctrl+Shift+O` pour auto-importer |
| `AsyncTask` soulignée en jaune | API dépréciée | Normal pour ce lab débutant — ignorer l'avertissement |
| L'app se ferme au lancement | Erreur dans `onCreate` | Vérifier les IDs XML (`R.id.txtStatus`, etc.) |

---

## Auteur & contexte

Ce lab a été réalisé dans le cadre du cours de **développement mobile Android**.
Il illustre les bases de la programmation concurrente sur Android avant d'aborder
des solutions modernes comme les Coroutines Kotlin ou WorkManager.
