#!/bin/bash
curl -X DELETE https://music.brino.dk/api/v1/playlists/{id} \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json"
