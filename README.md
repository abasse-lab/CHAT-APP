# Chat application

## Réalisé par BRISSARD Alexis, CAMARA Abasse, EL RIFAI Rami

### Initialisation

En fonction du dossier de destination des fichiers compilés, il est possible d'être amené à changer les chemins des dossiers suivants :
* le dossier avec les données persistées (historiques, utilisateurs) : changer la variable `PERSIST_DATA_FOLDER` dans le fichier `Server.java`
* le dossier avec les ressources utilisées pour l'IHM (fonts, img) : changer la variable `ASSETS_FOLDER` dans le fichier `ChatFrame.java`

### Compilation

#### Compilation avec Maven

```
mvn compile -f pom.xml
```


#### Compilation sans Maven

* Télécharger la librairie `json-simple-1.1.1.jar`
* Compiler les fichiers `.java`

### Exécution

#### Serveur

```
java server.Server <port>
```

Attention : si le projet est compilé avec Maven via un IDE, il est peut-être plus judicieux de lancer l'exécution en passant par l'IDE.

#### Client

```
java client.Client <adresse IP serveur> <port>
```