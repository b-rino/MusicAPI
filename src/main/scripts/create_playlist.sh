#!/bin/bash
curl -X POST https://music.brino.dk/api/v1/playlists \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "INSERT_PLAYLIST_NAME"
  }'
