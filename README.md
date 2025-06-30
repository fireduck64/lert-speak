# lert-speak
Speak alerts from Unifi NVR to Chromecast speakers

## Overview

Uses NVR local API websockets to listen for events.

When an event is received, uses elevenlabs to render a short message to a voice MP3.
Or can be configured to use festival for local (and less fancy text to audio).

Then sends that to local chromecast devices.

See conf/lertspeak-example.conf


