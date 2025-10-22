#!/bin/bash
curl -X GET https://music.brino.dk/api/v1/playlists \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json"
