#!/bin/bash
curl -X POST https://music.brino.dk/api/v1/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "INSERT_USERNAME",
    "password": "INSERT_PASSWORD"
  }'
