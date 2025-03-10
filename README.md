in your local, pull the project and make a different git repo for config-repo, and copy its absolute path in the application.yml in the config-server.

## Pros
configurable through the config server
## Cons
needs to redeploy the app for changes to take affect from the config server.