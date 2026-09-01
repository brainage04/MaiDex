# Credential replacement required

`VaultCredentialTool` was intentionally removed from this project. Do not restore the Git submodule.

No active credential callsite was found outside the removed tool. Add no replacement until a caller needs credentials. If one is introduced, use a repository-ignored `.env.local` file with mode `0600`, load only the required named variables, and track a values-free `.env.example`. Never commit, log, or pass secret values on a command line.

Vaultwarden may remain the manual source of truth for updating those stable local values.
