<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/838b2041-5002-4a45-8019-eef1dce04331

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio) · [Ollama](https://ollama.com) (for Architect AI local inference)

### Architect AI — local LLM setup (offline, no API key required)

This app runs **Architect AI** — powered by `gemma4:e2b` via Ollama — entirely on-device.
All privacy and security analysis happens locally; no data is sent to any cloud service.

1. Install [Ollama](https://ollama.com) on your development machine
2. Pull the model: `ollama pull gemma4:e2b`
3. Start Ollama: `ollama serve` (it listens on `127.0.0.1:11434` by default)
4. The `.env` file is already preconfigured — no API key needed:
   ```
   ARCHITECT_AI_BASE_URL=http://10.0.2.2:11434
   ARCHITECT_AI_MODEL=gemma4:e2b
   ```
   - `10.0.2.2` is the Android emulator's alias for the host machine's loopback
   - On a physical device, replace with your machine's LAN IP

### Android Studio

1. Select **Open** and choose the directory containing this project
2. Allow Android Studio to fix any incompatibilities as it imports the project
3. Click **Sync Now** when the Gradle banner appears
4. Run the app on an emulator or physical device
