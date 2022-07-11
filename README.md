# Crawl Roster - Adventure League planning for dummies

## Local Dev

### Discord

Register an application in the developer portal - [https://discord.com/developers/applications](https://discord.com/developers/applications)

#### OAuth2 setup 
In the Oauth2 tab:
- Generate a new secret, save the client-id and secret in a secure place.
- Set in discord the OAuth redirect url to `http://localhost:8080/auth/discord/callback`

### Run Dev
To start the BE dev server the following env vars must be set:
```
DISCORD_CLIENT_ID=YOUR_ID
DISCORD_CLIENT_SECRET=YOUR_SECRET
ROSTER_DEV_MODE=true
```
Configure your IDE to start the gradle `run` with these vars set

Run the gradle `jsRun` task, this starts the FE dev server on localhost:8081.
Once running, run the `run` gradle task to start the BE.

When both are up and running you can access the site on [localhost:8080](http://localhost:8080)
