# lert-speak
Speak alerts from Unifi NVR to Chromecast speakers

## Overview

Uses NVR local API websockets to listen for events.

When an event is received, uses elevenlabs to render a short message to a voice MP3.

Then sends that to local chromecast devices.

See conf/lertspeak-example.conf


