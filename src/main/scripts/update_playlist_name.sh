#!/bin/bash
curl -X PUT https://music.brino.dk/api/v1/playlists/{} \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Evening Chill"
  }'
