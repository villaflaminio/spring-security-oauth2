# OAuth2 - Springboot

Spring Security è un framework del progetto Spring che consente di gestire in modo semplice e trasparente l’autenticazione (ovvero chi sei) e la profilazione (ovvero cosa sei autorizzato a fare) degli utenti che accedono ad una applicazione web.

API implementate:

![Untitled](OAuth2%20-%20Springboot%207008576a77c549b893f7f9d695b75f43/apiList.png)


Per abilitare l'accesso social con un provider OAuth2, dovrai creare un'app nella console del provider OAuth2 e ottenere ClientId e ClientSecret.

I provider OAuth2 utilizzano ClientId e ClientSecret per identificare la tua app. I fornitori richiedono anche molte altre impostazioni che includono -

- **URI di reindirizzamento autorizzati** : questi sono l'elenco valido di URI di reindirizzamento in cui un utente può essere reindirizzato dopo aver concesso/rifiutato l'autorizzazione alla tua app. Questo dovrebbe puntare all'endpoint dell'app che gestirà il reindirizzamento.
- **Ambito** : gli ambiti vengono utilizzati per chiedere agli utenti il permesso di accedere ai propri dati.

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

![Untitled](OAuth2%20-%20Springboot%207008576a77c549b893f7f9d695b75f43/Untitled.png)

## **Flusso di accesso OAuth2**

- Il flusso di accesso OAuth2 verrà avviato dal client inviando una richiesta all'endpoint `http://localhost:8080/oauth2/authorize/{provider}?redirect_uri=<redirect_uri_after_login>`.
    
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

![Untitled](OAuth2%20-%20Springboot%207008576a77c549b893f7f9d695b75f43/Untitled%201.png)

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

```http request
refreshTokenRepository.deleteByExpiryDateIsLessThan(Instant.now().plusMillis(Long.parseLong(Objects.requireNonNull( env.getProperty("app.auth.refreshTokenExpiration"))) + 1000));
```

# **Flusso per recupero password**

Per recuperare la password l’untente potra’ ricevere una email fornendo la propria, se la registrazione e’ avvenuta attraverso OAuth2 dovra’ eseguire l’accesso con quel provider.

Per richiedere la mail con la nuova password , fornendo come parametro la mail:

```http request
http://localhost:8080/api/auth/recoveryPassword?email=f.villa@elis.org
```

L’utente ricevera’ una mail con questa struttura

![Untitled](OAuth2%20-%20Springboot%207008576a77c549b893f7f9d695b75f43/Untitled%202.png)

Cliccando sul link dovra’ essere fatto il redirect ad una paggina fe , che richiedera’ il JWT di sessione attraverso :

```http request
http://localhost:8080/api/auth/tokenResetPassword?token=e0ecb5ef-a3e4-4f49-a43f-59e5f6ee54e2
```

in cui passera’ come paramentro il token che e’ stato ricevuto via email, come response ottiene il JWT

Per il reset della password aggiungere nel body “newPassword” 

```http request
http://localhost:8080/api/user/changePassword
```

Per procedere con l’autenticazione:

[http://localhost:8080/oauth2/authorize/google?redirect_uri=http://localhost:8080/oauth2/callback/google](http://localhost:8080/oauth2/authorize/google?redirect_uri=http://localhost:8080/oauth2/callback/google)