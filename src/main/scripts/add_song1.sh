#!/bin/bash
curl -X POST https://music.brino.dk/api/v1/playlists/{id}/songs \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": 123456,
    "title": "Lose Yourself",
    "artist": "Eminem",
    "album": "8 Mile"
  }'
