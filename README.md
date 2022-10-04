# OAuth2 - Springboot

Spring Security è un framework del progetto Spring che consente di gestire in modo semplice e trasparente l’autenticazione (ovvero chi sei) e la profilazione (ovvero cosa sei autorizzato a fare) degli utenti che accedono ad una applicazione web.

## API implementate:

![Untitled](screenshot/Screenshot_api.png)


# Start & Configurazione

Il progetto prevede 3 file di configurazione

- application.properties
- application-dev.properties
- application-prod.properties

Il file di configurazione di default e’ [application.properties](http://application.properties) , deve quindi contenere tutti i parametri di configurazione comuni.

E’ possibile scegliere il profilo da utilizzare specificando nel file [application.properties](http://application.properties) :

`spring.profiles.active=prod`

1) Creare il MySQL / MariaDB database

mysql> create database `spring_security`

2) Configurare database username and password

```java
spring.datasource.url=jdbc:mysql://localhost:3306/spring_security?useSSL=false&serverTimezone=UTC&useLegacyDatetimeCode=false
spring.datasource.username=root
spring.datasource.password=admin
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
```

3) Aggiungere i dati di OAuth2 Provider ClientId's and ClientSecrets

```java
spring.security.oauth2.client.registration.google.clientId= ...
spring.security.oauth2.client.registration.google.clientSecret= ... 
spring.security.oauth2.client.registration.google.redirectUri=${beUrl}/oauth2/callback/{registrationId}
spring.security.oauth2.client.registration.google.scope=email, profile
spring.security.oauth2.client.provider.google.authorizationUri=https://accounts.google.com/o/oauth2/v2/auth?prompt=consent&access_type=offline
spring.security.oauth2.client.registration.google.authorization-grant-type=authorization_code
```

4) Configurare redirect Uri su google developer console

Assicurati che `http://localhost:8080/oauth2/callback/<provider>`sia aggiunto come uri di reindirizzamento autorizzato nel provider OAuth2. Ad esempio, nella tua [console API di Google](https://console.developers.google.com/projectselector/apis/credentials?pli=1), assicurati che `http://localhost:8080/oauth2/callback/ google` viene aggiunto negli URI di reindirizzamento autorizzato.

Inoltre, assicurati che gli scope  siano aggiunti nella console del provider OAuth2. Ad esempio, gli ambiti `email` e `profile` dovrebbero essere aggiunti nella schermata di consenso OAuth2 del tuo progetto Google.

5) Configurare le credenziali per invio email

```java
mail.username= ...
mail.password= ...
spring.mail.host= ...
spring.mail.port= ...
spring.mail.username= ..
spring.mail.password= ...
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

6) Per avviare il progetto da linea di comando :  mvn`SecurityApplication`:run

Altrimenti Per avviarlo come container docker :

6.1) Creare un docker file

```java
FROM openjdk:17-oracle
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

6.2) Git clone `url_del_progetto`

6.3) mvn clean install

6.4) docker build -t spring_security_oauth2 .

6.5) docker run --restart=always -d --name spring_security_oauth2 -e TZ=Europe/Rome -p 4005:8080 spring_security_oauth2

Per arrestare /  rimuovere il container

```
docker stop spring_security_oauth2
docker rm spring_security_oauth2
docker rmi $(docker images | grep 'spring_security_oauth2')

```

# Riassunto flusso OAuth2

1)Il frontend chiama il backend con :
URL_BE/oauth2/authorize/google?redirect_uri=URL_REDIRECT_FE

2)Il be chiama google fornendo la sua client_id e richiede i dati dell'utente a fine autenticazione

esempio:
[https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=35513996-i5m1r8dd2rm3pt8k74r5nrv7eo0bnfdv.apps.googleusercontent.com&scope=email profile&state=sEU48FnolsngIKk7A1jU3Tx0YYfp9X4JfKahFLY-dtA%3D&redirect_uri=https://securityproject.flaminiovilla.it/oauth2/callback/google](https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=361885213996-i5m1r8dd2rm3pt8k74r5nrv7eo0bnfdv.apps.googleusercontent.com&scope=email%20profile&state=sEU48FnolsngIKk7A1jU3Tx0YYfp9X4JfKahFLY-dtA%3D&redirect_uri=https://securityproject.flaminiovilla.it/oauth2/callback/google)

2.1) chiaramente il redirect_uri e' quello che impostiamo nella cloudconsole di

1. il be riceve la outh dello user e chiede i dati a google con
   [https://accounts.google.com/signin/oauth/consent?authuser=0&part=AJi8hAN](https://accounts.google.com/signin/oauth/consent?authuser=0&part=AJi8hAN)....
2. il be riceve i dati e genera il token
3. il be effettua il redirect sul url specificato dalla richiesta 1) (URL_REDIRECT_FE) , restituendo come parametro il token:
   URL_REDIRECT_FE?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE2NjQ2MTgzNjksImF1ZCI6InNlY3VyZS1hcHAiLCJzdWIiOiIzIiwiZXhwIjoxNjY0NzA0NzY5LCJyb2wiOlsiUk9MRV9VU0VSIl19.i4b9c3vm2njeYoGPxo44Uw5X1hTxXi3JENl-Ipz9dlpGCNamzoAOQTb9O2FnhBWrwUlw9wfhgvRb8E1t1O86iw
4. Il token e’ un vero JWT, e per ottenere le altre informazioni di sessione occorre chiamare l’api

   /api/aut/session


## Modello utente :

![Untitled](screenshot/Untitled.png)

Risposta autenticazione utente

```json
{
    "id": 1,
    "email": "admin@flaminiovilla.it",
    "name": "admin",
    "role": [
        {
            "authority": "ROLE_ADMIN"
        },
        {
            "authority": "ROLE_USER"
        }
    ],
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE2NjMzMTYwODYsImF1ZCI6InNlY3VyZS1hcHAiLCJzdWIiOiIxIiwiZXhwIjoxNjYzNDAyNDg2LCJyb2wiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVVNFUiJdfQ.d0PW7IADpiUILyS0A9hWXIG22JmaNtvyPCd56PZgTr6l7vShjB8oZT0dfHeE-jhgQXeleh08-viLmGk7NcSLUw",
    "refreshToken": "0dfc9f5d-0a9e-4493-b06e-22b2cffabb59",
    "duration": "86400000"
}
```

Per abilitare l'accesso social con un provider OAuth2, dovrai creare un'app nella console del provider OAuth2 e ottenere ClientId e ClientSecret.

I provider OAuth2 utilizzano ClientId e ClientSecret per identificare la tua app. I fornitori richiedono anche molte altre impostazioni che includono -

- **URI di reindirizzamento autorizzati** : questi sono l'elenco valido di URI di reindirizzamento in cui un utente può essere reindirizzato dopo aver concesso/rifiutato l'autorizzazione alla tua app. Questo dovrebbe puntare all'endpoint dell'app che gestirà il reindirizzamento.
- **Ambito** : gli ambiti vengono utilizzati per chiedere agli utenti il permesso di accedere ai propri dati.

Gestione credenziali :

Sono previsti diversi file di configurazione in base a l’ambiente di sviluppo (tes / prod ) , per configurarlo :

```bash
spring.datasource.url=jdbc:mysql://172.17.0.1:3306/spring_security?useSSL=false&serverTimezone=UTC&useLegacyDatetimeCode=false
spring.datasource.username=flaminio
spring.datasource.password= *********
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
beUrl = https://securityproject.flaminiovilla.it
feUrl =https://18b0-2-35-169-207.ngrok.io
app.oauth2.authorizedRedirectUris = *
```

authorizedRedirectUris = * (Ogni url di redirect e’ autorizzato)

### **Creazione di app Facebook, Github e Google**

- **App di Facebook** : puoi creare un'app di [Facebook dalla dashboard delle app di Facebook](https://developers.facebook.com/apps)
- **App Github** : le app Github possono essere create da [https://github.com/settings/apps](https://github.com/settings/apps) .
- **Google Project** : vai su [Google Developer Console](https://console.developers.google.com/) per creare un Google Project e le credenziali per OAuth2.

Occorre aggiungere queste credenziali nel file di properties di springboot

```csharp
spring.security.oauth2.client.registration.google.clientId=egergerg.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.clientSecret=dfgdfgdfgwBUd9
spring.security.oauth2.client.registration.google.redirectUri={baseUrl}/oauth2/callback/{registrationId}
spring.security.oauth2.client.registration.google.scope=email, profile
spring.security.oauth2.client.registration.facebook.clientId=13934233333333
spring.security.oauth2.client.registration.facebook.clientSecret=6742dfgdfg86ecbb9f17fgdfgdfgdf
spring.security.oauth2.client.registration.facebook.redirectUri={baseUrl}/oauth2/callback/{registrationId}
spring.security.oauth2.client.registration.facebook.scope=email, public_profile
...
```

## **Abilitazione CORS**

Abilitiamo CORS in modo che il FE possa accedere alle API da un'origine diversa. Ho abilitato l'origine `http://localhost:3000`poiché è lì che verrà eseguita la nostra applicazione frontend.

## **SecurityConfig**

La seguente classe SecurityConfig è il punto cruciale della nostra implementazione di sicurezza. Contiene configurazioni sia per l'accesso social OAuth2 che per l'accesso basato su e-mail e password.

![Untitled](screenshot/Untitled%201.png)

## **Flusso di accesso OAuth2**

- Il flusso di accesso OAuth2 verrà avviato dal client inviando una richiesta all'endpoint : `URL_BE/oauth2/authorize/google?redirect_uri=URL_REDIRECT_FE`

Il client inviera' una richiesta al be e specifichera' l'indirizzo in cui fare il redirect:

Esempio: [https://securityproject.flaminiovilla.it/oauth2/authorize/google?redirect_uri=FE_URL/oauth2/redirect](https://securityproject.flaminiovilla.it/oauth2/authorize/google?redirect_uri=https://18b0-2-35-169-207.ngrok.io/oauth2/redirect)

Da google l'unico indirizzo che puo' fare tutto il processo di login e' il backend

![Untitled](screenshot/Untitled%202.png)

Poi chiaramente ci sono i cors, quindi l'applicativo accetta richieste di auth solo da un determinato sorgente
Anche per quanto riguarda il redirect ho previsto che da be si specifica l'url di redirect autorizzato

Il `provider`parametro del percorso è uno di `google`, `facebook`, o `github`. 

`Redirect_uri`l'URI a cui l'utente verrà reindirizzato una volta che l'autenticazione con il provider OAuth2 avrà esito positivo. 

- Alla ricezione della richiesta di autorizzazione, il client OAuth2 di Spring Security reindirizzerà l'utente all'AutorizzazioneUrl del file `provider`.

                                                                                                                                                                                                             Tutto lo stato relativo alla richiesta di autorizzazione viene salvato utilizzando il `authorizationRequestRepository`parametro specificato in SecurityConfig.

                                                                                                                                                                                                             L'utente ora consente/nega l'autorizzazione alla tua app sulla pagina del provider. Se l'utente consente l'autorizzazione all'app, il provider reindirizzerà l'utente all'URL di richiamata `http://localhost:8080/oauth2/callback/{provider}`con un codice di autorizzazione. Se l'utente nega l'autorizzazione, verrà reindirizzato allo stesso callbackUrl ma con un'estensione `error`.

- Se il callback OAuth2 genera un errore, Spring security richiamerà il valore `oAuth2AuthenticationFailureHandler`specificato in `SecurityConfig`.
- Se la richiamata OAuth2 ha esito positivo e contiene il codice di autorizzazione, Spring Security scambierà `authorization_code`con an `access_token`e richiamerà il `customOAuth2UserService`specificato in SecurityConfig sopra.
- Recupera i `customOAuth2UserService`dettagli dell'utente autenticato e crea una nuova voce nel database o aggiorna la voce esistente con la stessa email.
- Infine, `oAuth2AuthenticationSuccessHandler`viene invocato il. Crea un token di autenticazione JWT e genera l’ogetto di autenticazione. aggiungendolo nella response :

```json
{
   "id":10,
   "email":"admin@gmail.com",
   "name":"elis",
   "role":[
      {
         "role":"ROLE_USER"
      }
   ],
   "token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE2NjMwODYxNTYsImF1ZCI6InNlY3VyZS1hcHAiLCJzdWIiOiIxMCIsImV4cCI6MTY2Mzk1MDE1Niwicm9sIjpbIlJPTEVfVVNFUiJdfQ.HgRvINmHyisQY5xoGHMLNfcjbJkoyf5bb7nJotkIhQClLHgwrRwNvtgknTfVLw0oVUaLf86oONMpUwMReC1f7w",
   "refreshToken":"65285eb9-339b-4f55-8700-492d6178ef28",
   "duration":"864000000"
}
```

### **Gestore degli errori di autenticazione OAuth2**

In caso di errore durante l'autenticazione OAuth2, Spring Security invoca il `onAuthenticationFailure()`metodo `OAuth2AuthenticationFailureHandler`che abbiamo configurato in `SecurityConfig`.

# **Flusso per refresh token Spring Boot con JWT**

Il diagramma mostra il flusso di come implementiamo il processo di autenticazione con token di accesso e token di aggiornamento.

![Untitled](screenshot/Untitled%203.png)

`refreshToken`verrà fornito al momento dell'accesso dell'utente.

Il token di aggiornamento ha valore e tempo di scadenza diversi rispetto al token di accesso.

### Flusso per il refresh del token

Nel `refreshtoken()`metodo:

- In primo luogo, otteniamo il token di aggiornamento dai dati della richiesta
- Quindi, ottengo l’oggetto di refresh dal token utilizzando `RefreshTokenService`
- Verifichiamo il token (scaduto o meno) in base al campo`expiryDate`
- Utilizziamo il campo `userRefreshTokenJwtUtils`dell'oggetto come parametro per generare un nuovo token di accesso, che verra’ salvato a db tramite la repository.
- Quando viene creato un nuovo refresh token, il service `RefreshTokenService`
- Restituisco `TokenRefreshResponse` Oppure lancia`TokenRefreshException`

Un therad task ogni 5 minuiti elimina i refreshToken da database piu’ vecchi dell’expirationDate

```java
refreshTokenRepository.deleteByExpiryDateIsLessThan(Instant.now().plusMillis(Long.parseLong(Objects.requireNonNull( env.getProperty("app.auth.refreshTokenExpiration"))) + 1000));
```

# **Flusso per recupero password**

Per recuperare la password l’untente potra’ ricevere una email fornendo la propria, se la registrazione e’ avvenuta attraverso OAuth2 dovra’ eseguire l’accesso con quel provider.

Per richiedere la mail con la nuova password , fornendo come parametro la mail:

```json
http://localhost:8080/api/auth/recoveryPassword?email=f.villa@elis.org
```

L’utente ricevera’ una mail con questa struttura

![Untitled](screenshot/Untitled%204.png)

Cliccando sul link dovra’ essere fatto il redirect ad una paggina fe , che richiedera’ il JWT di sessione attraverso :

```json
http://localhost:8080/api/auth/tokenResetPassword?token=e0ecb5ef-a3e4-4f49-a43f-59e5f6ee54e2
```

in cui passera’ come paramentro il token che e’ stato ricevuto via email, come response ottiene il JWT

Per il reset della password aggiungere nel body “newPassword”

```json
http://localhost:8080/api/user/changePassword
```

# Postman Test api

```json
https://www.getpostman.com/collections/37450aa7b8497023b66f
```